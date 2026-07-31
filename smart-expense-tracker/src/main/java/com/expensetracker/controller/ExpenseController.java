package com.expensetracker.controller;

import com.expensetracker.dto.CategoryTotalResponse;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.TotalResponse;
import com.expensetracker.model.Expense;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@Tag(name = "Expenses", description = "Manage personal expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @Operation(summary = "Add a new expense")
    public ResponseEntity<Expense> addExpense(@Valid @RequestBody ExpenseRequest request) {
        Expense created = expenseService.addExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "View all expenses")
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single expense by id")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter expenses by category")
    public ResponseEntity<List<Expense>> getExpensesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(expenseService.getExpensesByCategory(category));
    }

    @GetMapping("/total")
    @Operation(summary = "Get the total of all expenses")
    public ResponseEntity<TotalResponse> getTotal() {
        return ResponseEntity.ok(new TotalResponse(expenseService.getTotal()));
    }

    @GetMapping("/total/{category}")
    @Operation(summary = "Get the total of expenses within a category")
    public ResponseEntity<CategoryTotalResponse> getCategoryTotal(@PathVariable String category) {
        double total = expenseService.getCategoryTotal(category);
        return ResponseEntity.ok(new CategoryTotalResponse(category, total));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense by id")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
