# Smart Expense Tracker API

A REST API for tracking personal expenses — add, list, filter by category,
compute totals (overall and per-category), and delete. Built with Java 21
and Spring Boot. No database: data is kept in memory and mirrored to a JSON
file (`expenses.json`) so it survives a restart.

## Features

- Add an expense
- View all expenses
- Filter expenses by category
- Total of all expenses
- Total of expenses in a given category
- Delete an expense by id
- Bean Validation on input (title, amount, category, date)
- Centralized error handling (404 for missing expenses, 400 for invalid input)
- OpenAPI / Swagger UI for interactive API exploration (bonus feature)
- 8 MockMvc tests covering the happy paths and the error paths

## Requirements

- Java 21 (JDK)
- Maven 3.9+ (or use the included `mvnw` wrapper if you add one — plain `mvn` is assumed below)

## Installation

```bash
git clone <your-repo-url>
cd smart-expense-tracker
mvn clean install
```

This compiles the project, runs the test suite, and packages a runnable jar.

## Run the server

```bash
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

Alternatively, after `mvn clean install`:

```bash
java -jar target/smart-expense-tracker-1.0.0.jar
```

Data is persisted to `expenses.json` in the working directory and reloaded
automatically the next time the app starts.

## Run the tests

```bash
mvn test
```

Tests use MockMvc against the real Spring context, with an isolated data
file (`target/test-expenses.json`) so they never touch your real
`expenses.json`. See [`tests/README.md`](tests/README.md) for where the
test suite physically lives (Maven requires `src/test/java`).

## Interactive API docs (Swagger UI)

With the app running, visit:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI JSON is available at `http://localhost:8080/v3/api-docs`.

## API Endpoints

### Add an expense

```
POST /expenses
Content-Type: application/json

{
  "title": "Food",
  "amount": 250,
  "category": "Food",
  "date": "2026-07-31"
}
```

`201 Created` with the saved expense (including its generated `id`).

### View all expenses

```
GET /expenses
```

`200 OK` with a JSON array of expenses, sorted by id.

### Get a single expense

```
GET /expenses/{id}
```

`200 OK` with the expense, or `404 Not Found` if the id doesn't exist.

### Filter by category

```
GET /expenses/category/{category}
```

`200 OK` with a JSON array (empty array if nothing matches). Matching is
case-insensitive.

### Total of all expenses

```
GET /expenses/total
```

```json
{ "total": 3500.0 }
```

### Total by category

```
GET /expenses/total/{category}
```

```json
{ "category": "Food", "total": 1200.0 }
```

Returns `"total": 0.0` if the category has no expenses (not a 404 — it's a
valid, empty query result).

### Delete an expense

```
DELETE /expenses/{id}
```

`204 No Content` on success, `404 Not Found` if the id doesn't exist.

## Validation & error responses

Invalid input (`POST /expenses`) returns `400 Bad Request`:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "timestamp": "2026-07-31T10:15:30Z",
  "fieldErrors": {
    "title": "title must not be blank",
    "amount": "amount must be greater than 0"
  }
}
```

Rules enforced:
- `title`: required, not blank
- `amount`: required, must be > 0
- `category`: required, not blank
- `date`: required, format `yyyy-MM-dd`

A missing expense (`GET/DELETE /expenses/{id}`) returns `404 Not Found`:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Expense not found with id: 42",
  "timestamp": "2026-07-31T10:15:30Z"
}
```

## Project Structure

```
smart-expense-tracker/
├── README.md
├── AI_NOTES.md
├── pom.xml
├── tests/
│   └── README.md              # pointer to the real test suite
└── src/
    ├── main/
    │   ├── java/com/expensetracker/
    │   │   ├── SmartExpenseTrackerApplication.java
    │   │   ├── controller/ExpenseController.java
    │   │   ├── service/ExpenseService.java
    │   │   ├── model/Expense.java
    │   │   ├── dto/ExpenseRequest.java
    │   │   ├── dto/TotalResponse.java
    │   │   ├── dto/CategoryTotalResponse.java
    │   │   └── exception/
    │   │       ├── ExpenseNotFoundException.java
    │   │       ├── GlobalExceptionHandler.java
    │   │       └── ErrorResponse.java
    │   └── resources/application.properties
    └── test/
        ├── java/com/expensetracker/ExpenseControllerTest.java
        └── resources/application-test.properties
```

## Design notes

- **Model vs. request DTO**: `Expense` (the stored/returned shape) is kept
  separate from `ExpenseRequest` (the POST body) so a client can never set
  or override the server-generated `id`.
- **Persistence**: a single `ExpenseService` bean holds an `ArrayList` in
  memory and writes the full list to `expenses.json` after every add/delete,
  loading it back in on startup via `@PostConstruct`. A `ReentrantLock`
  guards read-modify-write sequences since the bean is a singleton shared
  across concurrent requests.
- **Category matching** is case-insensitive (`Food` and `food` are treated
  as the same category) since that's what a real user would expect.
- **`/expenses/total/{category}`** returns `0` for an unknown category
  rather than `404`, because "total spent in a category with no expenses"
  is a legitimate answer, not an error.
