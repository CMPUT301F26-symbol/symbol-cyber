# Sprint 1

Planning started October 23, 2026. Lab review: **Tuesday, October 27**. Part 3 checkpoint: **Friday, November 6, 2026, 5:00 PM** (America/Edmonton).

**Process owner:** Dean Elbery (@EazyDean), [T8 #93](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/93).

Published version: [Sprint 1 wiki](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Sprint-1). [Product Backlog](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Product-Backlog) · [Project board](https://github.com/orgs/CMPUT301F26-symbol/projects/2).

## Sprint goal and scope

Demonstrate **owner creates a campsite → entrant sees that campsite in the list → the same record is visible in the Firestore console**. Use the shared Firebase project and real app reads/writes.

Assignments below reflect Dean's October 27 confirmation and existing GitHub assignments. Status is a snapshot as of October 27; owners update the issue, board, and wiki together as work progresses. A branch, placeholder screen, or research spike does not complete a feature story.

## Task owners and dependencies

| Task | Owner | Tracking / deliverable | Status and next step |
| --- | --- | --- | --- |
| T1 Project skeleton | Abhishek Mishra (@AByte-voyager) | [Merged PR #91](https://github.com/CMPUT301F26-symbol/symbol-cyber/pull/91) | Done: merged October 25; Kotlin/Compose skeleton, build instructions, Firebase configuration guidance. |
| T2 Firebase + Firestore | Fahad Khan (@Fahada-Code) | [#94](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/94), `t2-firebase`, wiki page **Firestore Data Model** | In Progress: assigned and branch exists; real read/write and data-model page are still to be verified. Coordinate fields with T3. |
| T3 User + device identification | Noor Alam (@nooralam15) | [#22](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/22), User model, KDoc, JUnit test | In Progress: CRC design exists; implementation and tests are not yet merged. Settle the schema with T2 before dependent models. |
| T4 Navigation | Aalok Mishra (@Mishra411) | [Merged PR #92](https://github.com/CMPUT301F26-symbol/symbol-cyber/pull/92) | Done for the shell: merged October 27. PR reports 16 passing JUnit tests. Role filtering still depends on T3; placeholders do not complete stories. |
| T5 Register campsite | Dean Elbery (@EazyDean) | [#27](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/27), [#28](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/28), `t5-owner` | Todo — waiting for T2 and T3. Dean will implement after those owners complete the shared foundation. |
| T6 Browse campsites | Noor (#3), Dean (#4), Aalok (#5) | Individual story table below | Todo: current main has navigation placeholders. Integrate the shared Campsite schema and Firestore reader when T2/T3/T5 are ready. |
| T7 Risk spikes (half day) | Fahad Khan (@Fahada-Code) | [#95](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/95); QR #61/#25 and notifications #9/#77 | Todo: record candidate libraries, actual test results, risks, and Hanzheng Dai's library approval in a short wiki note. |
| T8 Process | Dean Elbery (@EazyDean) | [#93](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/93), `t8-process` PR, this page | In Progress until the documentation PR is merged; assignments and board reflect the plan. |

## Stories planned this sprint

| Story | Issue | Owner | Sprint scope | Status |
| --- | --- | --- | --- | --- |
| US 01.09.01 — identified by device | [#22](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/22) | Noor Alam (@nooralam15) | T3: User identity and roles, KDoc, realistic JUnit data | In Progress |
| US 02.01.01 — register campsite | [#27](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/27) | Dean Elbery (@EazyDean) | T5: registration form saved to Firestore | Todo; depends on T2/T3 |
| US 02.01.02 — provide campsite details | [#28](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/28) | Dean Elbery (@EazyDean) | T5: details, Campsite model, KDoc and tests | Todo; depends on T2/T3 |
| US 01.01.03 — campsite list reflects filters | [#3](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/3) | Noor Alam (@nooralam15) | T6: Firestore list, filter integration, empty state | Todo |
| US 01.01.04 — campsite summaries | [#4](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/4) | Dean Elbery (@EazyDean) | T6: name, location, capacity, rating/placeholder | Todo |
| US 01.01.05 — campsite details | [#5](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/5) | Aalok Mishra (@Mishra411) | T6: full details and optional-image placeholder | Todo |
| US 02.02.01 — generate campsite QR | [#61](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/61) | Fahad Khan (@Fahada-Code) | T7: spike only | Todo; full story stays open after spike |
| US 01.12.01 — scan campsite QR | [#25](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/25) | Fahad Khan (@Fahada-Code) | T7: spike only | Todo; full story stays open after spike |
| US 01.03.01 — winner notification | [#9](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/9) | Fahad Khan (@Fahada-Code) | T7: spike only | Todo; full story stays open after spike |
| US 02.10.01 — notify waiting list | [#77](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/77) | Fahad Khan (@Fahada-Code) | T7: spike only | Todo; full story stays open after spike |

Existing related assignments remain unchanged: Amr Soliman (@awsolim) owns [search #1](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/1), Fahad owns [filters #2](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/2), and Abhishek owns [join from details #6](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/6). These are follow-up integration work; the October 27 demo does not establish their completion. #3 needs #1/#2 behavior before its full acceptance criteria can pass.

## Shared User / role decision

The existing [User CRC card](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/CRC-Cards) holds device identification, profile information, notification preferences, policy access, profile deletion, and a **set of roles**. Entrant, CampsiteOwner, and Admin are collaborating role cards. One User can hold multiple roles; switching modes selects an applicable role on the same profile. This records the current CRC decision; Noor owns its implementation and consistency with T2's Firestore schema.

PR #92 currently exposes all three modes until T3 is integrated. Do not treat that temporary navigation behavior as completed role authorization or completion of US 03.11.01 (#89).

## Part 2 feedback addressed

Source: Hanzheng Dai's October 20 team feedback; checked against the repository/wiki on October 27. These are current corrections, not a claim that they all existed before the Part 2 deadline or that a marker has approved them.

| Feedback | Correction / evidence | Owner | Current result |
| --- | --- | --- | --- |
| Backlog wiki only mirrored 30 of 60 stories | [Product Backlog](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Product-Backlog) now contains 26 entrant, 24 owner, and 10 admin stories; all 60 issue links are present. Summary: 165 points, 30 halfway stories. | Fahad / Amr | Addressed in current wiki. |
| Default Home page and missing Tool Use page | [Home](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki) contains team/project content and deliverable links; [Tool Use](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Tool-Use) exists. | Dean | Addressed. Sprint navigation and feedback record added in T8. |
| README links pointed to OWNER/REPOSITORY; AI disclosure and progress were placeholders | [README](https://github.com/CMPUT301F26-symbol/symbol-cyber/blob/main/README.md) uses actual repository links and contains AI-tool disclosures and percentage estimates for CRC/backlog/UI; Home has populated progress rows. | Dean; deliverable authors own disclosures | Addressed for the reported placeholders. Authors remain responsible for the accuracy of their disclosures. |
| CRC numbering jumped from section 2 to 5 | [CRC Cards](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/CRC-Cards) now uses sections 1, 2, and 3. | Noor | Addressed. |
| Shared profile/device ID must support multiple roles | User CRC owns identity/profile/roles and lists Entrant, CampsiteOwner, and Admin as collaborators. See the decision above and [#22](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/22). | Noor | Design recorded; T3 implementation and T2 schema alignment remain open. |
| E00 email/password and forgot-password conflict with device identification | [E00](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/UI-Mockups#e00-sign-in) PNG and notes show device recognition and "Continue with this device"; no credential fields. [S1](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Storyboards) labels those actions; S2 uses the same profile for mode switching. | Aalok / Abhishek | Addressed in inspected PNGs and notes. |
| Remove unsupported price/refund from mockups | Inspected E05 details, E07 booking, E10 cancellation, and O02 form PNGs have no price/refund controls; current mockup notes also omit them. | Aalok / Abhishek | Addressed in those mockups. Follow-up: #5's existing description still mentions price; Aalok should reconcile it with the project specification before implementation. |

No additional Part 2 marking feedback has been verified in this record. Add any later feedback with its source, an owner, evidence, and an honest status.

## Completion and board workflow

- Each source file starts with a short comment explaining its purpose and known issues. Add KDoc to model classes and their public methods.
- Use Kotlin official style and Android Studio → Reformat Code. Model and controller unit tests use realistic data, including relevant invalid and missing-data cases.
- Work on your own branch, open a PR to `main`, and reference the issue. Use `Closes #27` only when the full story is complete; use `Refs #27` for partial work. No rebase or force push.
- Each member commits code each week using their own account. Wiki edits alone do not satisfy this Part 3 requirement. Dean checks contribution evidence at the weekly review; do not commit for a teammate.
- **Todo** means planned, including work waiting on a dependency. **In Progress** means active work or review. Record the blocker on the issue/wiki because the board has no separate blocked column. **Done** requires merged work, passing relevant checks, and acceptance evidence.
- When a story is done, close its GitHub issue, move its board card to Done, and mark its row Done in the wiki Product Backlog with the PR/test evidence. Update this sprint page too. Do not close feature stories merely because T7 research finished.
- Dean maintains the board and wiki at handoff and review; each story owner supplies progress, blockers, and evidence. Current assignments are a plan, not evidence that all members have already met the weekly code-commit requirement.

## October 27 demo verification

These checks are pending until the owners run the integrated app. Record the tested commit, device/emulator, result, and evidence link in #94/#27/#3 before marking the demo complete.

- [ ] T2/T3 share an agreed User and Campsite schema; the app uses the shared Firebase project.
- [ ] Owner opens the T5 form and creates a realistic campsite, for example "Jasper Riverside", location "Jasper", maximum occupancy 4, amenities and tags.
- [ ] Firestore console shows the saved campsite document and its values; record the document ID as evidence.
- [ ] Entrant opens the T6 list and sees that same campsite from Firestore, including the expected summary fields.
- [ ] Entrant opens its details; optional-image and unrated states work without crashing.
- [ ] Invalid required input does not create a campsite; a read/write failure produces useful UI feedback.

At the October 27 snapshot, T1/T4 are merged; the complete Firestore demo has **not been verified** by this process update.

## Weekly contribution check

Known merged code evidence: Abhishek's T1 commit `f10b092` (October 25), Aalok's T4 commit `2025ec0` (October 26), and Amr's merge of #92 (October 27). Dean will check each member's code contribution across branches and merged PRs at review; this is not a certification that every member has already contributed code this week.

AI assistance: Codex (OpenAI) helped audit the current records and draft/update this T8 process documentation and assignments. No teammate's code contribution is attributed to this documentation work.
