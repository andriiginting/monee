# Monee Mobile Design

Status: approved prototype direction  
Last updated: September 5, 2026

## Product intent

Monee is a household finance app for two equal members managing money across
multiple accounts, currencies, savings goals, investments, and reconciliation
records.

The mobile experience should make the daily workflow fast:

1. Understand the household position.
2. Record or scan a transaction.
3. Confirm what will enter the ledger.
4. See whether the household plan is on track.

Advanced reporting remains available, but it must not dominate the home screen.

## Chosen design direction

Use **Material 3 as the structural design system** for the shared Compose
Multiplatform UI.

Use Spotify's high-contrast visual principles as inspiration:

- Strong dark and light themes.
- Green as the primary action and progress accent.
- Platform-default sans-serif typography.
- Clear hierarchy with minimal decorative chrome.
- Legible labels and metadata.

Reference:
[Spotify Design and Branding Guidelines](https://developer.spotify.com/documentation/design)

This is inspiration, not a Spotify clone. Monee must not use Spotify's logo,
waves, naming, or other identifying brand elements. The prototype currently
uses Spotify Green (`#1ED760`) as a provisional accent. Before public release,
replace it with a distinct Monee brand green and validate the final palette for
accessibility and brand differentiation.

### Why not Liquid Glass

Liquid Glass is not the primary system because Monee is a shared KMP product,
not an Apple-only application. Making it foundational would create avoidable
iOS/Android divergence, increase implementation cost, and make shared UI
behavior harder to keep consistent.

Platform-specific effects may be used sparingly where they improve native
integration, but they must not change information architecture or core flows.

## Themes and color

Monee supports Light, Dark, and System theme settings.

| Token | Light | Dark | Purpose |
| --- | --- | --- | --- |
| App background | `#F7F8F6` | `#101512` | Primary application surface |
| Surface | `#FFFFFF` | `#181F1B` | Cards, sheets, and navigation |
| Tonal surface | `#EDF1ED` | `#202923` | Secondary containers |
| Primary text | `#121713` | `#F6FAF7` | Titles, values, and essential content |
| Secondary text | `#626D66` | `#A7B0AA` | Supporting labels and metadata |
| Border | `#DCE4DE` | `#2C3730` | Quiet structural separation |
| Prototype accent | `#1ED760` | `#1ED760` | Actions, progress, and active state |
| Accent content | `#07110B` | `#07110B` | Content displayed on the accent |

Rules:

- Color must never be the only indicator of state.
- Financial errors and conflicts use explicit text and icons.
- Green indicates actions, progress, and positive status—not arbitrary
  decoration.
- Large financial values require sufficient contrast in both themes.
- Theme changes must not restart navigation or destroy screen state.

## Typography

Use the default sans-serif font for each platform through Material 3 typography.

- Prioritize legibility over visual novelty.
- Use tabular figures for aligned and changing financial values.
- Use no more than three levels of emphasis on a screen.
- Supporting text must remain readable at accessibility font scales.
- Avoid extra-bold text across the whole interface; reserve strong emphasis for
  balances, totals, and primary headings.

## Navigation

The primary mobile navigation is a **docked, edge-to-edge Material navigation
bar**. It is not a floating capsule.

Primary destinations:

1. Home
2. Activity
3. Budget
4. Goals
5. Insights

Rules:

- The bar is attached to the bottom edge and respects system safe areas.
- Active state uses a small tonal indicator behind the icon plus a text label.
- Navigation state survives process recreation.
- Temporary workflows such as receipt review, account editing, and exchange
  open as sheets or focused full-screen flows, not new primary destinations.

## Home screen hierarchy

The home screen answers: “How is our household doing, and what do I need to do
next?”

Order:

1. Household identity and sync state.
2. Household net worth with hide/show control.
3. JPY/IDR display-currency switch.
4. Horizontally scrollable account groups.
5. Daily actions.
6. Current budget status.
7. Next savings goal.
8. Recent activity.

Daily actions:

- Add expense
- Scan receipt
- Exchange or transfer
- Reconcile

The home screen must not become an advanced-report dashboard. Detailed
allocation, exposure, valuation, and historical reports belong in Insights.

## Accounts and money presentation

- Accounts are real financial containers.
- Savings goals are allocations and must not pretend to be bank accounts.
- Account cards show name, current value, account count or type, and freshness
  of the latest balance or rate.
- Multi-currency totals clearly show the reporting currency.
- Historical values use dated FX rates.
- Missing rates produce an explicit incomplete state rather than a fabricated
  total.
- Balances may be hidden without hiding non-sensitive navigation labels.

## Transactions and receipt OCR

Manual entry and receipt scanning share the same confirmation model.

Receipt flow:

`Capture → OCR → editable draft → explicit confirmation → ledger`

Rules:

- OCR never posts automatically.
- The review surface shows merchant, total, currency, category, account, date,
  and confidence or ambiguity.
- The primary action states exactly what will happen: **Confirm and post**.
- Canceling or closing a draft must not create a ledger transaction.
- Offline saves show a visible pending sync state.

## Exchange and transfer flow

Exchange is a focused full-screen flow.

It shows:

- Source account and available balance.
- Sell currency and amount.
- Receive currency and calculated amount.
- Dated FX rate.
- Fees when applicable.
- A review step before confirmation.

The dated rate is stored with the transaction so later rate changes do not
rewrite historical reports.

## Components and interaction

- Use Material 3 components and semantics before creating custom primitives.
- Minimum touch target is 44×44 points or 48×48 dp where platform guidance
  requires it.
- Use bottom sheets for short editable tasks.
- Use full-screen flows for complex, consequential tasks.
- Avoid excessive elevation and shadows.
- Cards should group real information, not decorate empty space.
- Loading, offline, pending, failed, and conflicted states are first-class UI
  states.
- Financial conflicts never resolve silently.

## Accessibility

- Support screen readers with descriptive labels for financial values and
  controls.
- Support dynamic text without clipping balances or actions.
- Pair status colors with text and icons.
- Respect reduced-motion settings.
- Preserve logical focus order in sheets and full-screen flows.
- Verify contrast for both themes before accepting a component.

## KMP implementation guidance

- Build the shared UI with Compose Multiplatform and Material 3.
- Define Monee semantic tokens instead of scattering raw colors through
  composables.
- Keep theme, typography, shape, and component tokens in the shared UI module.
- Keep platform-specific visual effects optional and isolated.
- Use shared navigation destinations and screen state; platform adapters handle
  permissions, OCR, and other native capabilities.
- The prototype is a design reference, not production architecture or source
  code.

## Prototype acceptance criteria

The mobile design direction is accepted when:

- Light and dark themes are both usable.
- The bottom navigation is docked rather than floating.
- The home hierarchy works at 320–430 px widths.
- Daily actions are reachable with one tap.
- Receipt drafts cannot post without confirmation.
- Two-member household and sync state are visible.
- JPY and IDR amounts remain readable without clipping.
- Advanced reporting remains discoverable without crowding daily workflows.

