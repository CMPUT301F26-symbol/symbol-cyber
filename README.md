# CMPUT 301 - Symbol

## Project Overview

This repository is for our CMPUT 301 team project Symbol. 

## Project Documentation

All deliverables are maintained in the repository's **GitHub Wiki**.

| Deliverable | Documentation | Responsible members |
| --- | --- | --- |
| Product Backlog | Wiki: [Product Backlog](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Product-Backlog) | Fahad Khan, Amr Soliman |
| UI Mockups and Storyboards | Wiki: [UI Mockups](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/UI-Mockups) and [Storyboards](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Storyboards) | Aalok Mishra, Abhishek Mishra |
| CRC Cards | Wiki: [CRC Cards](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/CRC-Cards) | Noor Alam |
| Wiki Structure and Tool Use | Wiki: [Tool Use](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Tool-Use) | Dean Elbery |

**[Open the Wiki Home page](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/CMPUT-301-%E2%80%90-Symbol)**  
**[GitHub Project board](https://github.com/orgs/CMPUT301F26-symbol/projects/2)**

### Sprint 1 process

The [Sprint 1 wiki](https://github.com/CMPUT301F26-symbol/symbol-cyber/wiki/Sprint-1)
records this week's stories and owners, dependencies, current progress, the
October 27 demo checks, and **Part 2 feedback addressed**. A versioned copy is
kept in [doc/sprint-1.md](doc/sprint-1.md). Track process work in
[T8 #93](https://github.com/CMPUT301F26-symbol/symbol-cyber/issues/93) and keep
the issue, Project board, and wiki backlog aligned as stories finish.

### Deliverable Requirements

**Product Backlog**
- One GitHub Issue per required user story, including its description, explanation, acceptance criteria, story points, and risk.
- Part 3 halfway-checkpoint stories clearly identified.
- Every issue on the Project board is summarized in the wiki.

**UI Mockups and Storyboards**
- Annotated mockups of main screens and major dialogues, labelled by user story.
- Storyboards covering entrant, campsite owner, and administrator flows, with actions labelled on every transition arrow.
- PNG images embedded directly in the wiki and a user-story-to-screen mapping.

**CRC Cards**
- CRC cards for the important domain classes, identifying responsibilities, collaborators, and parent classes where applicable.
- A mapping confirming each user story is supported by at least one class.
- Consistent class and story naming across the backlog, mockups, and CRC cards.

**GitHub Tool Use**
- Organized wiki navigation to all four deliverables.
- Updated README and `.gitignore`.
- Individual commits or wiki edits, and at least one assigned issue per member.
- Current issue statuses on the Project board.

## Team Members

| Member | Responsibility |
| --- | --- |
| Fahad Khan | Product Backlog |
| Amr Soliman | Product Backlog |
| Aalok Mishra | UI Mockups and Storyboards |
| Abhishek Mishra | UI Mockups and Storyboards |
| Noor Alam | CRC Cards |
| Dean Elbery | Wiki Structure and Tool Use, README, `.gitignore`, contribution checks |

## Collaboration Guidelines

1. Commit code or documentation using **your own GitHub account**. Do not commit on behalf of a teammate.
2. Each member must be assigned at least one GitHub Issue; update its Project board status as work progresses.
3. Keep user story identifiers consistent across all planning deliverables.
4. Do not rebase or force-push shared branches. Configure Git with `git config pull.rebase false`.
5. Record instructor feedback and final-week action items in the wiki.
6. Keep all final Part 2 submission materials in the team repository and wiki. External links to mockups do not replace embedded PNGs.

## Important Dates

| Event | Date and time |
| --- | --- |
| Team progress meeting | October 13 or 14, 2026, during the regular lab section |
| **Part 2 deadline** | **October 20, 2026, 5:00 PM** |
| Individual peer review (Canvas) | October 21, 2026, 5:00 PM |
| Part 3 halfway checkpoint | November 6, 2026 |

## AI Tool Use Disclosure

**AI tool used:** ChatGPT (OpenAI).

**How it was used:** To help draft and format the GitHub Wiki Home page, a CRC Cards page with illustrative examples, and this README. The team is responsible for reviewing, correcting, and finalizing all content in accordance with the official project specification. In particular, sample CRC cards must not be treated as verified project analysis.

**AI Tool Use in the CRC Cards (45%)**
GPT 6.1-Sol was used to assist in the formulation of the CRC Cards. It was used to help extract the appropriate classes from the User Story (5%). It was also used to help formula the cards for Reservation, Rating, Image, Map, Notifcation, Message, and AvailablilitySchedule (40%)

“I am creating CRC cards for the following software description problem. These are the classes I identitifed, is this correct? Entrant, Campsite, Lottery, WaitingList, Notification, AvailableDates,Reservation, CampsiteOwner, QRCode, Message, Rating, User (Abstract) Admin” prompt. ChatGPT, model GPT-6.1 Sol, OpenAI, 18 Oct. 2026, chatgpt.com. (5%)

“I have completed the detailed CRC Cards for the Entrant, CampsiteOwner, Admin, Campsite, Lottery, and WaitingList classes. Please complete the remaining classes for Reservation, Rating, Image, Map, Notifcation, Message, and AvailablilitySchedule (Not sure if this should be included). Ensure that cards are One to One (If A lists B, B should list A). Also do a check to ensure that all User Stories are mentioned in the final classes. I have yet to include a handful of the User stories including the ones such as reservation confirmation, notidcation of reservation cancellation etc. Do a final check” prompt. ChatGPT, model GPT-6.1 Sol, OpenAI, 18 Oct. 2026, chatgpt.com. (40%)

**AI tool used:** Claude Opus 4.8

**How it was used:** Claude Opus 4.8 was used to format the user stories. We provided an example user story as a template, along with our team's guidelines for estimating story points and assigning risk levels. The AI helped assist with consistent descriptions, acceptance criteria, and story point and risk estimates for the remaining user stories. The team is responsible for reviewing, correcting, and finalizing the generated content.

**AI Tool use in the Project Backlog (50%)**
Claude Opus 4.8 was used to help draft and format the user stories. We provided the structure of US 01.01.01 as an example and instructed the AI to follow the same format for the remaining stories. We also provided our story point estimation scale and risk definitions so that the estimates would remain consistent across the project.

"Use the following user story as a template for the remaining user stories. Follow the same format, including the user story title, description, acceptance criteria, and a table containing the actor, story points, risk, and Halfway (Part 3) MVP status." Claude, model Claude Opus 4.8, Anthropic, 12 Oct, 2026, claude.ai. (40%)

"Use the following guidelines when assigning story points and risk levels to each user story. Story points are based on the amount of work, complexity, and uncertainty involved. Use a scale of 1, 2, 3, 5, and 8 points. Risk should be evaluated separately based on technical uncertainty. High risk involves unfamiliar technologies, medium risk involves Firestore data structures that have not yet been designed, and low risk involves straightforward features. Apply these guidelines consistently when creating the user stories." Claude, model Claude Opus 4.8, Anthropic, 12 Oct, 2026, claude.ai. (10%)

**AI Tool Use in the UI Mockups and Storyboards (60%)**
Claude (Anthropic) was used to help with the UI mockup images, screen descriptions, how the storyboard sequences should be, and the user story-to-screen traceability table. We provided directions on how the user stories should connect to the screens, reviewed the generated content, and asked Claude to check the work and suggest possible improvements. We decided which suggestions to use, added the Device Sign In screen, and uploaded the final files to the project wiki.

"Help me develop UI mockups and the flow of storyboard sequences based on the information I provided. Make sure the screens connect logically to the user stories. I will review the results and provide further direction. Also, suggest any changes that could improve the mockups and storyboards." Claude, Anthropic, 8 Oct. 2026, claude.ai. (40%)
  
"Review the user story-to-screen traceability table and check whether each user story is connected to the correct screens. Identify missing connections, inconsistencies, or improvements, and suggest changes for me to review." Claude, Anthropic, 19 Oct. 2026, claude.ai. (20%)

---

*Project documentation is maintained collaboratively by the CMPUT 301 team.*

## Build and run

Requirements: Android Studio, the Android SDK components requested by
the project, and a compatible emulator or Android device.

1. Clone the repository:
   ```bash
   git clone https://github.com/CMPUT301F26-symbol/symbol-cyber.git
   ```
2. In Android Studio, select File → Open and open the `code` folder.
3. Wait for Gradle sync to finish. Install any requested SDK components.
   Android Studio creates the local `local.properties` file.
4. Select a compatible emulator or connected Android device.
5. Press Run.

### Getting google-services.json

The initial T1 skeleton does not use Firebase and runs without this file.
Firebase integration will be added in T2.

The teammate responsible for T2 creates the shared Firebase project
using a personal Google account, as instructed in the Firestore lab,
and registers our Android application.

After the Firebase project is configured:

1. Obtain `google-services.json` from the teammate managing Firebase.
   Alternatively, if you have access to the shared Firebase project,
   open Project settings → General → Your apps, select our Android app,
   and download `google-services.json`.
2. Place the file at `code/app/google-services.json`.
   It belongs inside the app module, not the repository or project root.
3. Sync the project with Gradle in Android Studio.

Use the team's shared Firebase project; do not create a separate
Firebase project for each teammate.

Firebase dependencies, database setup, and read/write functionality
are handled in T2.
