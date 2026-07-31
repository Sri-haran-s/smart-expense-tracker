# Tests

This project follows the standard Maven/Spring Boot layout, so the actual
test suite lives at:

```
src/test/java/com/expensetracker/ExpenseControllerTest.java
```

Maven only discovers tests under `src/test/java`, so that's where they're
kept (moving them here would break `mvn test`). This folder exists to match
the requested top-level `tests/` structure.

Run them with:

```
mvn test
```
