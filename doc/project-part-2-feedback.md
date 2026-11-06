# Project Part 2 Feedback

This page records feedback on the Part 2 project deliverables, the responsible members, corrections and outstanding follow-ups. Weekly sprint scope, assignments and lab reviews are maintained separately in [Sprint Planning and Reviews](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Sprint-Planning-and-Reviews).

| Record | Detail |
| --- | --- |
| Feedback source | Hanzheng Dai's October 20, 2026 team feedback, as recorded in Sprint 1 |
| Correction/evidence snapshot | October 27, 2026 |
| Page separated | November 6, 2026 |
| Coordinator | Dean Elbery; each deliverable owner maintains their own corrections |

The existing feedback record is moved here without claiming a new TA review or a fresh audit of the corrections. Statuses below retain their October 27 meaning; later implementation changes do not retroactively change that snapshot.

## Recorded feedback and responses

Source: Hanzheng Dai's October 20 team feedback; checked against the repository/wiki on October 27. These are current corrections, not a claim that they all existed before the Part 2 deadline or that a marker has approved them.

| Feedback | Correction / evidence | Owner | Current result |
| --- | --- | --- | --- |
| Backlog wiki only mirrored 30 of 60 stories | [Product Backlog](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Product-Backlog) now contains 26 entrant, 24 owner, and 10 admin stories; all 60 issue links are present. Summary: 165 points, 30 halfway stories. | Fahad / Amr | Addressed in current wiki. |
| Default Home page and missing Tool Use page | [Home](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki) contains team/project content and deliverable links; [Tool Use](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Tool-Use) exists. | Dean | Addressed. Sprint navigation and feedback record added in T8. |
| README links pointed to OWNER/REPOSITORY; AI disclosure and progress were placeholders | [README](https://github.com/CMPUT301F26-symbol/symbol-cyber/blob/main/README.md) uses actual repository links and contains AI-tool disclosures and percentage estimates for CRC/backlog/UI; Home has populated progress rows. | Dean; deliverable authors own disclosures | Addressed for the reported placeholders. Authors remain responsible for the accuracy of their disclosures. |
| CRC numbering jumped from section 2 to 5 | [CRC Cards](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/CRC-Cards) now uses sections 1, 2, and 3. | Noor | Addressed. |
| Shared profile/device ID must support multiple roles | User CRC owns identity/profile/roles and lists Entrant, CampsiteOwner, and Admin as collaborators. See the [recorded Sprint 1 role decision](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Sprint-1#shared-user--role-decision) and [#22](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/22). | Noor | Design recorded; T3 implementation and T2 schema alignment remain open. |
| E00 email/password and forgot-password conflict with device identification | [E00](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/UI-Mockups#e00-sign-in) PNG and notes show device recognition and "Continue with this device"; no credential fields. [S1](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Storyboards) labels those actions; S2 uses the same profile for mode switching. | Aalok / Abhishek | Addressed in inspected PNGs and notes. |
| Remove unsupported price/refund from mockups | Inspected E05 details, E07 booking, E10 cancellation, and O02 form PNGs have no price/refund controls; current mockup notes also omit them. | Aalok / Abhishek | Addressed in those mockups. Follow-up: #5's existing description still mentions price; Aalok should reconcile it with the project specification before implementation. |

No additional Part 2 marking feedback has been verified in this record. Add any later feedback with its source, an owner, evidence, and an honest status.

## Outstanding follow-ups in the recorded snapshot

- **Noor and Fahad:** verify that the implemented User/roles and Firestore model agree with the recorded multi-role design. The table's design correction is not evidence of completed implementation.
- **Aalok:** reconcile issue #5's price wording with the project specification; the inspected mockups already removed price/refund controls.
- **All deliverable owners:** maintain accurate AI-tool disclosures and evidence for corrections; do not label later changes as present before the Part 2 deadline.

Record any new TA feedback here with its actual date, owner, evidence and outcome. Record weekly story planning and lab discussions in the sprint pages instead.

AI assistance: Codex (OpenAI) helped organize the previously recorded feedback into this separate page. No new feedback, TA acceptance or correction verification is asserted by this reorganization.
