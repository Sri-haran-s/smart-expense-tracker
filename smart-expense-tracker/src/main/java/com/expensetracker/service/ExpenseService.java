package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.model.Expense;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Holds expenses in memory (ArrayList) and mirrors every change to a JSON
 * file on disk so data survives an application restart. There is no
 * database involved, per the assignment's requirements.
 *
 * A lock guards read-modify-write sequences since Spring beans are
 * singletons shared across concurrent HTTP requests.
 */
@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    private final ObjectMapper objectMapper;
    private final String dataFilePath;
    private final List<Expense> expenses = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final ReentrantLock lock = new ReentrantLock();

    public ExpenseService(ObjectMapper objectMapper,
                           @Value("${expense.tracker.data-file:expenses.json}") String dataFilePath) {
        this.objectMapper = objectMapper;
        this.dataFilePath = dataFilePath;
    }

    @PostConstruct
    void loadFromDisk() {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            log.info("No existing data file at {}. Starting with an empty expense list.", dataFilePath);
            return;
        }
        try {
            List<Expense> loaded = objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Expense.class));
            expenses.addAll(loaded);
            long maxId = expenses.stream().mapToLong(Expense::getId).max().orElse(0L);
            idGenerator.set(maxId);
            log.info("Loaded {} expense(s) from {}", expenses.size(), dataFilePath);
        } catch (IOException e) {
            log.warn("Could not read {} ({}). Starting with an empty expense list.", dataFilePath, e.getMessage());
        }
    }

    private void persist() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(dataFilePath), expenses);
        } catch (IOException e) {
            log.error("Failed to persist expenses to {}", dataFilePath, e);
        }
    }

    public Expense addExpense(ExpenseRequest request) {
        lock.lock();
        try {
            Expense expense = new Expense(
                    idGenerator.incrementAndGet(),
                    request.getTitle(),
                    request.getAmount(),
                    request.getCategory(),
                    request.getDate()
            );
            expenses.add(expense);
            persist();
            return expense;
        } finally {
            lock.unlock();
        }
    }

    public List<Expense> getAllExpenses() {
        lock.lock();
        try {
            return expenses.stream()
                    .sorted(Comparator.comparing(Expense::getId))
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public Expense getExpenseById(Long id) {
        lock.lock();
        try {
            return expenses.stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new ExpenseNotFoundException(id));
        } finally {
            lock.unlock();
        }
    }

    public List<Expense> getExpensesByCategory(String category) {
        lock.lock();
        try {
            return expenses.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().equalsIgnoreCase(category))
                    .sorted(Comparator.comparing(Expense::getId))
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public double getTotal() {
        lock.lock();
        try {
            return expenses.stream().mapToDouble(Expense::getAmount).sum();
        } finally {
            lock.unlock();
        }
    }

    public double getCategoryTotal(String category) {
        lock.lock();
        try {
            return expenses.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().equalsIgnoreCase(category))
                    .mapToDouble(Expense::getAmount)
                    .sum();
        } finally {
            lock.unlock();
        }
    }

    public void deleteExpense(Long id) {
        lock.lock();
        try {
            Optional<Expense> match = expenses.stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst();
            if (match.isEmpty()) {
                throw new ExpenseNotFoundException(id);
            }
            expenses.remove(match.get());
            persist();
        } finally {
            lock.unlock();
        }
    }
}
