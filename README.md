Monee :construction_worker::hammer:
=====

 <p align="center">
 <img src="/docs/monee.png"/>
 </p>

## Overview

Money Management is a cross-platform application built using Compose Multiplatform. The application 
helps me manage my finances by tracking expenses, incomes, and generating reports.

## Features

- Track daily expenses and incomes
- Categorize transactions
- Generate financial reports
- Sync data across multiple devices
- Secure authentication

## Design

The approved mobile design direction uses Material 3 structure, light and dark
themes, a high-contrast green accent, and a docked bottom navigation bar.

See [Monee Mobile Design](docs/design.md) for the canonical design decisions,
interaction rules, accessibility requirements, and KMP implementation guidance.


## Getting Started

### Prerequisites

- [JDK](https://adoptium.net/) 17+
- [Kotlin](https://kotlinlang.org/) 2.4.20
- [Compose Multiplatform](https://www.jetbrains.com/compose/) 1.12.0
- [Gradle](https://gradle.org/) 9.7.1 (provided by the Gradle wrapper)
- Android SDK Platform 36

### Contributing
I welcome contributions to this project! Please see the PR

## Firebase setup

Monee uses Firebase Authentication and Cloud Firestore through the Kotlin
Multiplatform Firebase SDK.

1. Create a Firebase project and enable Email/Password authentication.
2. Create a Firestore database.
3. Download `google-services.json` into `composeApp/`.
4. Add `GoogleService-Info.plist` and `GoogleService-Info-Debug.plist` to the
   iOS app target.
5. Publish [firestore.rules](firestore.rules) with the Firebase CLI.
6. Add Firebase Core, Auth, and Firestore to the iOS app through Swift
   Package Manager, then call `FirebaseApp.configure()` in `iOSApp`.

Build variants:

```text
Android debug:   com.andriiginting.moneyproject.debug  -> monee-debug
Android release: com.andriiginting.moneyproject        -> monee-27768
iOS debug:       com.andriiginting.moneyproject.MoneyProject.debug -> monee-debug
iOS release:     com.andriiginting.moneyproject.MoneyProject       -> monee-27768
```

Android selects `composeApp/src/debug/google-services.json` for debug builds
and `composeApp/google-services.json` for release builds. iOS selects the
debug plist under `DEBUG` and the release plist otherwise.

The data model is:

```text
users/{uid}                         -> householdId
households/{householdId}            -> name, memberIds[]
households/{householdId}/expenses/  -> household-owned expense documents
```

Do not ship the local repository as production auth. It remains available for
previews and tests until the Firebase project configuration files are present.
