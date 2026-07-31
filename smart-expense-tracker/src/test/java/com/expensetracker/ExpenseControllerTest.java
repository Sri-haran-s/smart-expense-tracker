package com.expensetracker;

import com.expensetracker.dto.ExpenseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests hitting the real Spring MVC stack via MockMvc.
 * Each test gets a fresh application context (DirtiesContext) so the
 * in-memory expense list never leaks state between tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ExpenseRequest sampleRequest(String title, double amount, String category, String date) {
        ExpenseRequest req = new ExpenseRequest();
        req.setTitle(title);
        req.setAmount(amount);
        req.setCategory(category);
        req.setDate(LocalDate.parse(date));
        return req;
    }

    @Test
    void addExpense_withValidPayload_returns201AndCreatedExpense() throws Exception {
        ExpenseRequest req = sampleRequest("Groceries", 250.0, "Food", "2026-07-31");

        mockMvc.perform(post("/expenses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Groceries"))
                .andExpect(jsonPath("$.amount").value(250.0))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void addExpense_withInvalidPayload_returns400WithFieldErrors() throws Exception {
        ExpenseRequest req = sampleRequest("", -10.0, "", "2026-07-31");

        mockMvc.perform(post("/expenses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.category").exists());
    }

    @Test
    void getAllExpenses_returnsEveryExpenseThatWasAdded() throws Exception {
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Lunch", 150.0, "Food", "2026-07-30"))));
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Uber", 300.0, "Transport", "2026-07-30"))));

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getExpensesByCategory_returnsOnlyMatchingCategory() throws Exception {
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Lunch", 150.0, "Food", "2026-07-30"))));
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Dinner", 350.0, "Food", "2026-07-30"))));
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Uber", 300.0, "Transport", "2026-07-30"))));

        mockMvc.perform(get("/expenses/category/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[1].category").value("Food"));
    }

    @Test
    void getTotal_returnsSumOfAllExpenseAmounts() throws Exception {
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Lunch", 150.0, "Food", "2026-07-30"))));
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Uber", 300.0, "Transport", "2026-07-30"))));

        mockMvc.perform(get("/expenses/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(450.0));
    }

    @Test
    void getCategoryTotal_returnsSumForOnlyThatCategory() throws Exception {
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Lunch", 150.0, "Food", "2026-07-30"))));
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Dinner", 350.0, "Food", "2026-07-30"))));
        mockMvc.perform(post("/expenses").contentType("application/json")
                .content(objectMapper.writeValueAsString(sampleRequest("Uber", 300.0, "Transport", "2026-07-30"))));

        mockMvc.perform(get("/expenses/total/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.total").value(500.0));
    }

    @Test
    void deleteExpense_withExistingId_returns204AndRemovesIt() throws Exception {
        String response = mockMvc.perform(post("/expenses").contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleRequest("Lunch", 150.0, "Food", "2026-07-30"))))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/expenses/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteExpense_withNonExistingId_returns404() throws Exception {
        mockMvc.perform(delete("/expenses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @AfterAll
    static void cleanupTestDataFile() {
        new File("target/test-expenses.json").delete();
    }
}
