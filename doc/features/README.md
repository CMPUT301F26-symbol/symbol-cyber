# Section A — Dean's campsite stories

The provided section A allocation leaves these five stories after excluding Fahad's assignments. Implementation assistance: OpenAI Codex. Each story has its own commit, branch, tests and notes. All five feature branches were merged into main individually on November 3, 2026. The feature branches were retained.

## Completed merge order

These stacked branches were merged in the order below, with one merge commit published at a time. Integration preserved the lottery and waiting-list work already on main.

| Order | Branch | Feature | Base |
| --- | --- | --- | --- |
| 1 | `US-01.01.01` | Search live campsites | `main` at `8ac6320` |
| 2 | `US-01.01.03` | Filtered results list | `US-01.01.01` |
| 3 | `US-01.01.04` | Campsite summaries | `US-01.01.03` |
| 4 | `US-01.01.05` | Entrant details and images | `US-01.01.04` |
| 5 | `US-02.01.04` | Owner details | `US-01.01.05` |

Git does not allow spaces in branch names. Hyphenated branch names preserve the story IDs.

## Integration still required

- Section B's US 01.01.02 must supply the selected filter criteria to `ExploreScreen(filters = ...)`. Location and guest matching are implemented. Date matching accepts campsite IDs from the lottery/availability lookup; the filter selectors and date lookup still need to be connected to the lottery data now on main. See [the filter contract](US-01.01.03.md).
- Fahad's owner forms can populate the added price, currency, rules and cancellation-policy fields. Older records show explicit placeholders. Registration already receives the active owner ID at the navigation boundary.
- Section E should pass the active `User.deviceId` to `AppNavHost` once its identity flow is connected. Existing blank-owner records need their correct owner ID assigned by the team; this work does not modify live data.

## Merge integration

The entrant detail destination retains the lottery-content slot and its existing join/leave waiting-list card. Owner details and registration share the existing device identity used by lotteries and notifications; `AppNavHost(ownerId = ...)` can override it when the user profile flow is connected. Both owner detail and owner waiting-list routes remain available.

## Validation

All 61 JVM tests pass after integration with the newer main branch. Debug app and instrumented-test APK builds pass. Android lint passes with warnings, no errors. Screen tests for search, filtering, summaries, entrant details and owner details compile; they have not run on a device because the installed emulator crashes before boot. Live Firestore access and successful remote image display are not claimed as verified.

From `code`, run:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
# With an emulator or device running:
./gradlew :app:connectedDebugAndroidTest
```

For this restricted build environment, the successful command also used a workspace `ANDROID_USER_HOME`, `-Pkotlin.compiler.execution.strategy=in-process` and `--no-watch-fs`. These environment-specific settings were not added to the shared build configuration.

Manual device checks: search/clear/no matches; return from details with the query retained; apply section B filters; inspect an optional-image campsite; update a displayed record from a second client; remove it; switch to owner mode and open a campsite owned by that device; confirm another owner's record cannot be opened through the owner route.
