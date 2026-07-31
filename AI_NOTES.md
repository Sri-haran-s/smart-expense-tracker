## AI Tools Used

- Claude AI
- ChatGPT

## 1. Which parts were AI-generated?

Claude AI was used to generate the initial Spring Boot project structure and most of the implementation, including:

- Project setup (Spring Boot + Maven)
- REST API controllers
- Service layer
- Model and DTO classes
- Exception handling
- Validation annotations
- Swagger/OpenAPI configuration
- Unit test templates
- README.md draft

## 2. What I validated, tested, or changed

I manually:

- Imported the project into IntelliJ IDEA.
- Configured and ran the Spring Boot application.
- Verified that the application started successfully.
- Tested all REST API endpoints using Swagger UI.
- Verified request validation and error handling.
- Confirmed the following endpoints work correctly:
  - Add Expense
  - View All Expenses
  - Get Expense by ID
  - Filter by Category
  - Get Total Expenses
  - Get Category Total
  - Delete Expense
- Reviewed the project structure to ensure it matched the assignment requirements.
- Fixed request payload issues during testing (such as validation errors caused by invalid input).

## 3. AI suggestions I chose not to use

Some AI suggestions were intentionally not used:

- Using a relational database (MySQL/H2) for persistence.
- Adding authentication and authorization.
- Implementing additional features beyond the assignment requirements.

These were omitted because the assignment explicitly allows in-memory or local JSON storage and focuses on a simple REST API.

## Reflection

AI accelerated the initial implementation and boilerplate generation. I was responsible for running the project, validating the API behavior, testing endpoints, understanding the code, and ensuring the final submission met the assignment requirements.
