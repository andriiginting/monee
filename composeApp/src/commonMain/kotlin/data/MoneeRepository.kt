package data

import data.ocr.ReceiptDraft
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class MoneeUser(
    val id: String,
    val email: String,
    val displayName: String,
)

@Serializable
data class Household(
    val id: String,
    val name: String,
    val memberIds: List<String>,
)

data class HouseholdInvite(
    val code: String,
    val householdId: String,
    val householdName: String,
    val expiresAtMillis: Long,
)

@Serializable
data class Expense(
    val id: String,
    val householdId: String,
    val createdBy: String,
    val amountMinor: Long,
    val currency: CurrencyType,
    val merchant: String,
    val category: String,
    val date: String?,
    val notes: String? = null,
    val createdAtMillis: Long = 0L,
)

data class MoneeSession(
    val user: MoneeUser,
    val household: Household,
)

interface MoneeRepository {
    val session: StateFlow<MoneeSession?>
    val sessionRestored: StateFlow<Boolean>
    val expenses: StateFlow<List<Expense>>

    suspend fun signIn(email: String, password: String): Result<MoneeSession>
    suspend fun createAccount(email: String, password: String): Result<MoneeSession>
    suspend fun createHouseholdInvite(): Result<HouseholdInvite>
    suspend fun joinHousehold(code: String): Result<MoneeSession>
    fun signOut()
    suspend fun addExpense(draft: ReceiptDraft): Result<Expense>
}

/**
 * App-scoped repository used until a real auth/database transport is selected.
 *
 * The important contract is already here: every expense carries householdId
 * and createdBy, and reads are scoped to the active household. This prevents
 * the UI from becoming coupled to a future backend SDK.
 */
class InMemoryMoneeRepository : MoneeRepository {
    private val _session = MutableStateFlow<MoneeSession?>(null)
    private val _sessionRestored = MutableStateFlow(true)
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())

    override val session: StateFlow<MoneeSession?> = _session.asStateFlow()
    override val sessionRestored: StateFlow<Boolean> = _sessionRestored.asStateFlow()
    override val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<MoneeSession> {
        val normalizedEmail = email.trim().lowercase()
        if (!normalizedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val userId = normalizedEmail.hashCode().toString()
        val user = MoneeUser(
            id = userId,
            email = normalizedEmail,
            displayName = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
        )
        val household = Household(
            id = "household-$userId",
            name = "${user.displayName}'s Household",
            memberIds = listOf(user.id),
        )
        val newSession = MoneeSession(user, household)
        _session.value = newSession
        return Result.success(newSession)
    }

    override suspend fun createAccount(email: String, password: String): Result<MoneeSession> =
        signIn(email, password)

    override suspend fun createHouseholdInvite(): Result<HouseholdInvite> {
        val session = _session.value ?: return Result.failure(IllegalStateException("Sign in first."))
        return Result.success(
            HouseholdInvite(
                code = "MONEE-${Random.nextInt(100, 999)}-${Random.nextInt(100, 999)}",
                householdId = session.household.id,
                householdName = session.household.name,
                expiresAtMillis = 0L,
            ),
        )
    }

    override suspend fun joinHousehold(code: String): Result<MoneeSession> =
        Result.failure(UnsupportedOperationException("Join household requires Firebase."))

    override fun signOut() {
        _session.value = null
    }

    override suspend fun addExpense(draft: ReceiptDraft): Result<Expense> {
        val currentSession = _session.value
            ?: return Result.failure(IllegalStateException("Sign in before posting an expense."))
        val amount = draft.totalMinor
            ?: return Result.failure(IllegalArgumentException("The receipt does not contain a total."))

        val expense = Expense(
            id = "expense-${Random.nextLong()}",
            householdId = currentSession.household.id,
            createdBy = currentSession.user.id,
            amountMinor = amount,
            currency = CurrencyType.JPY,
            merchant = draft.location ?: "Unknown merchant",
            category = draft.category,
            date = draft.date,
            createdAtMillis = kotlin.time.Clock.System.now().toEpochMilliseconds(),
        )
        _expenses.update { current -> current + expense }
        return Result.success(expense)
    }
}

class FirebaseMoneeRepository(
    private val auth: dev.gitlive.firebase.auth.FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : MoneeRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _session = MutableStateFlow<MoneeSession?>(null)
    private val _sessionRestored = MutableStateFlow(false)
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    private var expenseListenerJob: kotlinx.coroutines.Job? = null

    override val session: StateFlow<MoneeSession?> = _session.asStateFlow()
    override val sessionRestored: StateFlow<Boolean> = _sessionRestored.asStateFlow()
    override val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    init {
        scope.launch {
            auth.authStateChanged.collect { firebaseUser ->
                if (firebaseUser == null) {
                    expenseListenerJob?.cancel()
                    expenseListenerJob = null
                    _expenses.value = emptyList()
                    _session.value = null
                } else {
                    runCatching {
                        val restoredSession = loadOrCreateSession(firebaseUser)
                        _session.value = restoredSession
                        listenToExpenses(restoredSession.household.id)
                    }
                }
                _sessionRestored.value = true
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<MoneeSession> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim().lowercase(), password)
        val user = result.user ?: error("Firebase did not return an authenticated user.")
        val session = loadOrCreateSession(user)
        _session.value = session
        listenToExpenses(session.household.id)
        session
    }

    override suspend fun createAccount(email: String, password: String): Result<MoneeSession> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim().lowercase(), password)
        val user = result.user ?: error("Firebase did not return the new user.")
        val session = loadOrCreateSession(user)
        _session.value = session
        listenToExpenses(session.household.id)
        session
    }

    override suspend fun createHouseholdInvite(): Result<HouseholdInvite> = runCatching {
        val session = _session.value ?: error("Sign in before creating an invite.")
        val code = "MONEE-${Random.nextInt(100, 999)}-${Random.nextInt(100, 999)}"
        val expiresAt = kotlin.time.Clock.System.now().toEpochMilliseconds() + 24 * 60 * 60 * 1000
        firestore.collection("householdInvites").document(code).set(
            mapOf(
                "householdId" to session.household.id,
                "householdName" to session.household.name,
                "createdBy" to session.user.id,
                "expiresAtMillis" to expiresAt,
                "redeemedBy" to null,
            ),
        )
        HouseholdInvite(code, session.household.id, session.household.name, expiresAt)
    }

    override suspend fun joinHousehold(code: String): Result<MoneeSession> = runCatching {
        val currentUser = auth.currentUser ?: error("Sign in before joining a household.")
        val normalizedCode = code.trim().uppercase()
        val invite = firestore.collection("householdInvites").document(normalizedCode).get()
        if (!invite.exists) error("That invite code does not exist.")
        val expiresAt = invite.get<Long>("expiresAtMillis")
        if (kotlin.time.Clock.System.now().toEpochMilliseconds() > expiresAt) {
            error("That invite has expired.")
        }
        val householdId = invite.get<String>("householdId")
        val householdRef = firestore.collection("households").document(householdId)
        householdRef.update(
            mapOf(
                "memberIds" to FieldValue.arrayUnion(currentUser.uid),
                "lastJoinCode" to normalizedCode,
            ),
        )
        val household = householdRef.get()
        val user = MoneeUser(
            id = currentUser.uid,
            email = currentUser.email.orEmpty(),
            displayName = currentUser.displayName
                ?: currentUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: "Monee user",
        )
        val joinedSession = MoneeSession(
            user = user,
            household = Household(
                id = householdId,
                name = household.get<String>("name"),
                memberIds = household.get<List<String>>("memberIds"),
            ),
        )
        firestore.collection("users").document(currentUser.uid).set(
            mapOf(
                "email" to user.email,
                "displayName" to user.displayName,
                "householdId" to householdId,
            ),
        )
        _session.value = joinedSession
        listenToExpenses(householdId)
        joinedSession
    }

    override fun signOut() {
        expenseListenerJob?.cancel()
        expenseListenerJob = null
        _expenses.value = emptyList()
        _session.value = null
        scope.launch { auth.signOut() }
    }

    override suspend fun addExpense(draft: ReceiptDraft): Result<Expense> = runCatching {
        val currentSession = _session.value ?: error("Sign in before posting an expense.")
        val amount = draft.totalMinor ?: error("The receipt does not contain a total.")
        val expense = Expense(
            id = "expense-${Random.nextLong()}",
            householdId = currentSession.household.id,
            createdBy = currentSession.user.id,
            amountMinor = amount,
            currency = CurrencyType.JPY,
            merchant = draft.location ?: "Unknown merchant",
            category = draft.category,
            date = draft.date,
            createdAtMillis = kotlin.time.Clock.System.now().toEpochMilliseconds(),
        )
        firestore.collection("households")
            .document(currentSession.household.id)
            .collection("expenses")
            .document(expense.id)
            .set(expense)
        expense
    }

    private suspend fun loadOrCreateSession(firebaseUser: FirebaseUser): MoneeSession {
        val userRef = firestore.collection("users").document(firebaseUser.uid)
        val userSnapshot = userRef.get()
        val householdId = userSnapshot
            .takeIf { it.exists }
            ?.get<String>("householdId")
            ?: "household-${firebaseUser.uid}"
        val householdRef = firestore.collection("households").document(householdId)

        val householdName = if (userSnapshot.exists) {
            // Existing users are already household members, so this read is
            // allowed by the Firestore rules.
            householdRef.get().get<String>("name")
        } else {
            // New users are not members yet. Do not read the document first:
            // Firestore would correctly deny that read. Creation is allowed
            // because the request includes the authenticated uid as a member.
            "${firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "My"} Household"
        }

        if (!userSnapshot.exists) {
            householdRef.set(
                mapOf(
                    "name" to householdName,
                    "memberIds" to listOf(firebaseUser.uid),
                ),
            )
        }
        val displayName = firebaseUser.displayName
            ?: firebaseUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Monee user"
        val user = MoneeUser(firebaseUser.uid, firebaseUser.email.orEmpty(), displayName)
        userRef.set(
            mapOf(
                "email" to user.email,
                "displayName" to user.displayName,
                "householdId" to householdId,
            ),
        )
        return MoneeSession(
            user = user,
            household = Household(
                id = householdId,
                name = householdName,
                memberIds = if (userSnapshot.exists) {
                    householdRef.get().get<List<String>>("memberIds")
                } else {
                    listOf(firebaseUser.uid)
                },
            ),
        )
    }

    private fun listenToExpenses(householdId: String) {
        expenseListenerJob?.cancel()
        expenseListenerJob = scope.launch {
            firestore.collection("households")
                .document(householdId)
                .collection("expenses")
                .snapshots
                .collect { snapshot ->
                    _expenses.value = snapshot.documents.map { document ->
                        document.data<Expense>().copy(
                            id = document.id,
                            householdId = householdId,
                        )
                    }
                }
        }
    }
}

object MoneeStore {
    // Firebase is the production adapter. Use InMemoryMoneeRepository directly
    // in previews/tests that do not provide native Firebase configuration.
    val repository: MoneeRepository by lazy { FirebaseMoneeRepository() }
}

fun List<Expense>.forHousehold(householdId: String?): List<Expense> =
    if (householdId == null) emptyList() else filter { it.householdId == householdId }
