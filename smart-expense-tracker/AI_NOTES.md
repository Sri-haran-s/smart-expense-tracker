# AI Notes

## AI Usage

I used Claude to generate the full first draft of this project: the Maven
setup, the domain model, the service layer with JSON-file persistence, the
controller, validation annotations, the global exception handler, the
README, and the MockMvc test suite.

Specifically, AI-generated:
- `pom.xml` (dependency choices and versions)
- `Expense`, `ExpenseRequest`, `TotalResponse`, `CategoryTotalResponse`
- `ExpenseService` (in-memory list + JSON file persistence + locking)
- `ExpenseController` (all six required endpoints)
- `ExpenseNotFoundException`, `ErrorResponse`, `GlobalExceptionHandler`
- `ExpenseControllerTest` (8 MockMvc tests)
- `README.md`

## >>> Fill in before submitting <<<

This section needs to reflect what **you** actually did, not what the AI
did — the assignment is explicitly grading that, so don't leave it generic.
Before submitting, actually:

1. Run `mvn clean install` and `mvn test` on a clean checkout and confirm
   they pass.
2. Start the server and exercise each endpoint yourself (curl, Postman, or
   Swagger UI at `/swagger-ui.html`) — POST an expense, list it, filter by
   category, check both total endpoints, delete it, and try an invalid
   payload to see the 400 response.
3. Read through `ExpenseService`, `ExpenseController`, and
   `GlobalExceptionHandler` line by line so you can explain any part of
   them in an interview.

Then replace this section with your own notes, e.g.:

**What I validated / changed:**
- (e.g. "Ran the full suite — all 8 tests passed on first run" or note
  anything you had to fix)
- (e.g. "Tested manually with curl — confirmed category filtering is
  case-insensitive, confirmed 404 on deleting a missing id")
- (e.g. "Changed X because...")

**AI suggestions I rejected:**
- Used H2/an embedded database — rejected because the assignment explicitly
  asks for no database; a JSON file plus an in-memory list satisfies "data
  survives restarts" without one.
- (add anything else you changed or declined)
