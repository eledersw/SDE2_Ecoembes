# Ecoembes — Central Server (Software Design, Univ. Deusto 2026-27)

## Context
University team project, course "Software Design". Current milestone: **Prototype TW1 = Central Server only**.
- Submission deadline: **Mon 9 Nov 2026, 10:00** (ALUD).
- Formal Technical Review: **Tue 10 Nov 2026**. Every team member must explain the work and demo it live.
- The user (Eleder) is new to Java, Spring Boot and Gradle. His background is TypeScript, Next.js, Supabase and Docker. Explain what you do in terms he can repeat in the review, and map concepts to that background when useful. Prefer simple, conventional, readable code over clever code.

## Sources of truth (read before writing any code)
- `docs/Ecoembes_ProblemDescription.pdf`: official brief, covering functionality, TW1 tasks and the assessment checklist.
- `docs/api-table.*`: the team's Central Server API design (part 1, done by teammates). **Endpoints, HTTP methods, paths, inputs and outputs MUST match it.**
- `docs/class-diagram.png`: team's class diagram (part 2). **Class names, attributes, methods and packages MUST match it.**
- `docs/sequence-diagram.png`: component-level sequence diagram (part 3).
- Reference implementation from the lecturer: https://github.com/rcarball/auctions-server-1. Clone it into `../reference/auctions-server-1` and treat it as read-only. **Mirror its package structure, annotation style, Swagger setup, build.gradle and Spring Boot version** (Spring Boot 4.x, springdoc-openapi). The course explicitly expects code organised like the reference case.
- **Java version: JDK 25** (the course installation guide for 2026-27 requires it). The reference repo's README still says 21. If its build.gradle pins a Java toolchain/sourceCompatibility of 21, use 25 in our project instead.

## Repository layout
Later prototypes add more projects (TW2: PlasSB server and ContSocket server; TW3: client). So each project lives in its own folder from day one:
```
/ecoembes-server/   <- TW1 Spring Boot + Gradle project (settings.gradle rootProject.name = folder name)
/docs/              <- brief, API table, diagrams, Swagger/Postman screenshots
```
`.gitignore` must exclude build outputs and IDE files (`build/`, `.gradle/`, `bin/`, `.idea/`, `.settings/`, `.classpath`, `.project`).

## TW1 hard requirements (from the official brief)
- Java project using **Spring Boot + Gradle** (Gradle wrapper included).
- **NO persistence**: no JPA, no H2, no database. All functionality is **simulated in the service layer** with in-memory data preloaded at startup. Include enough sample employees, dumpsters, readings and plants to demo every operation.
- External recycling plants (PlasSB, ContSocket) are **simulated inside the service layer** in TW1. No real calls to other servers yet; that comes in TW2.
- Patterns that must be clearly visible in the code:
  - **Remote Façade**: `@RestController` classes. Routing and HTTP concerns only, no business logic. They delegate to services.
  - **Application Service**: `@Service` classes containing the business logic.
  - **DTO + Assembler**: DTOs are the JSON shapes crossing the network. They hold no logic and don't depend on domain classes. Assembler methods convert domain objects to and from DTOs.
  - **State Management**: the server keeps in memory a `Map<token, Employee>` of logged-in employees (stateful session).
- **Login**: email + password produces a token equal to the server timestamp (long, millis) at login time. Every employee operation requires a valid token. Logout removes the token. Invalid or missing token returns 401. Use `ResponseEntity` with meaningful HTTP status codes (200/201/204/400/401/404).
- **Swagger**: every endpoint documented with springdoc annotations (`@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`). Swagger UI must work at `/swagger-ui/index.html`.
- Code must be fully functional, error-free and warning-free (`./gradlew build` clean).
- **Consistency between the API table, class diagram and code is graded.** If the code needs something the diagram or table doesn't have, or the diagram has something that doesn't make sense in code, **STOP and tell the user** so the team can update the diagram. Never silently diverge.

## Functionality (every item must be demo-able from Swagger or Postman)
- `updateDumpsterInfo`: sensor daily update (no employee token). Stores a new reading (numContainers, fillLevel GREEN/ORANGE/RED) so usage history exists.
- `login`, `logout`
- `createDumpster`
- `getDumpsterUsage(dumpsterId, startDate, endDate)`
- `getDumpsterStatusByArea(postalCode, date)`
- `getPlantCapacity(plantId, date)`: capacity in tons, simulated for PlasSB and ContSocket.
- `assignDumpstersToPlant(plantId, dumpsterIds)`: validates against plant capacity, records the assigning employee, and simulates the plant notification (e.g. a log line with number of dumpsters and total containers).
- `getAllDumpsters`, `getAllPlants`
- Optional, do NOT implement unless asked: confirmation email, saturation indicator.

## Open decisions: ask the user, never decide alone
1. Where the token travels for each endpoint (path variable / query param / header / body).
2. Login credentials as a JSON body (e.g. `CredentialsDTO`) vs. separate parameters.
3. Whether `updateDumpsterInfo` carries a date (needed to simulate history for usage queries) or the server stamps it.
4. How plant capacity (tons) relates to container counts when validating an assignment.

## Working style
- Work in small steps: domain classes, then DTOs + assemblers, then services (with simulated data), then façade controllers, then Swagger annotations. After each step, stop and give a short plain-language summary: what each class does, which pattern it implements, and how a request flows through it.
- Run `./gradlew build` after changes. It must compile with zero errors and warnings.
- Don't add features, libraries or layers beyond the brief.
- Small, descriptive commits. Don't push unless the user says so.
- **Never add Claude as co-author.** No `Co-Authored-By:` trailers, no "Generated with Claude Code" lines or similar attribution in commit messages, PR descriptions or code comments. Commits are authored solely by the user.
