# Campsite reviews and administration

The changes cover the November 24 assignment scope. Admin actions verify the refreshed Firebase `admin` claim. The production backend also needs to enforce those permissions.

| Story | App behavior | Acceptance boundary |
| --- | --- | --- |
| [#142](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/142) · US 01.13.02 | Entrant campsite details show reviews. An entrant can submit one text review per completed stay. A transaction verifies the saved stay and prevents duplicates. | Verify the identity binding and review write rule on a test Firebase project. |
| [#82](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/82) · US 03.02.01 | Confirmed removal replaces the selected profile with an inactive record containing the document identifier and removal time. It is excluded from the profiles list. The former device cannot recreate that profile through normal sign-in. | The inactive marker must be protected from client updates. Removal does not revoke the underlying Firebase Auth session or erase historical records. |
| [#83](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/83) · US 03.03.01 | Admin → Images confirms removal of a campsite image link. A transaction refuses to clear a different image uploaded after selection. | The app stores image URLs. Removing a reference does not delete media on the external host or retroactively clear other references to that URL. |
| [#87](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/87) · US 03.07.01 | Admin → Profiles can revoke campsite-owner access. The live profile subscription removes Owner mode on the affected device after Firestore confirms the change. Other roles and historical records are retained. | The server must reject owner-only writes from profiles without the role. Test with an owner session that was already open. |
| [#88](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/88) · US 03.08.01 | Admin → Logs → Notifications reads persisted notification records, with sender, recipient, content, time and lottery context. Includes empty, denied and retry states. | Server-side list access must be restricted to administrators. The log reflects recorded deliveries and does not fabricate records for opt-outs. |
| [#90](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/90) · US 03.12.01 | Admin → Logs → Reservation messages reads persisted reservation messages with owner, entrant and reservation identifiers. | The message producer is [#80](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/80). Live messages require its writer to use the contract below or an agreed adaptation. |

## Reservation message contract for issue #80

The message's authoritative document is `reservationMessages/{messageId}`. The admin log does not write messages.

| Field | Meaning |
| --- | --- |
| `reservationId` | ID from `reservations/{reservationId}` |
| `ownerId`, `entrantId` | Verified participant device IDs from that reservation |
| `campsiteName` | Display snapshot from the reservation |
| `message` | Message body |
| `createdAt` | Server timestamp. The log also accepts epoch milliseconds. |

Derive and validate both participants from the saved reservation. Delivery and logging should use the same document so a successful send always has an audit record. Existing notification messages to a lottery waitlist are a separate feature and do not appear in this log.

## Backend integration

No production rules are changed by this branch. The repository has no copy of the deployed Firestore rules. The existing Android device ID is a record key, so taking it from a user-supplied write is insufficient to prove identity. Use a trusted Firebase Auth mapping to associate the request with the correct device profile. The scoped example in [firestore-story-rules.example](../firestore-story-rules.example) uses server-provisioned `deviceId` and `admin` claims to illustrate the checks. The team can use an equivalent trusted binding.

Apply the authorization conditions to the project's existing rules after reviewing their overlapping grants. Owner-only write paths include campsite registration, editing, lottery publication and reservation rejection. Keep authorized draw and entrant operations separate. If a catch-all grant still permits the same write, a narrower rule does not restrict it. Do not allow ordinary clients to set profile roles or change the removal markers. Reservation writes also need to validate the lottery, entrant and booking period so a fabricated stay cannot grant review eligibility.

Before deploying, use a test project with a verified admin, an owner and two entrants. Confirm that regular users cannot read global logs or moderate records. Confirm that a revoked owner is rejected on owner-only writes, an inactive profile cannot be recreated, and review writes reject another entrant's reservation, a mismatched campsite, a future or rejected stay, and a second review for the same reservation. Verify that ordinary notification reads still show only the intended recipient's records.

AI assistance: Codex (OpenAI) assisted with the November 24 implementation, validation and documentation.
