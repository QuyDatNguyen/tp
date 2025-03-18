package budgetflow;

import budgetflow.expense.Expense;
import budgetflow.expense.ExpenseList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanceTrackerTest {
    public static final String EMPTY_EXPENSE_LIST_MESSAGE =
            "There is currently no expense in your list right now. Please add more expenses to continue";

    /**
     * Empties the persistent file "data/budgetflow.txt" and resets any static fields before each test.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Set the persistent file path relative to the project root.
        Path dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        Path filePath = dataDir.resolve("budgetflow.txt");
        if (Files.exists(filePath)) {
            Files.write(filePath, new byte[0]);
        } else {
            Files.createFile(filePath);
        }
        System.out.println("Cleared file: " + filePath.toAbsolutePath());

        // Reset static fields in ExpenseList, if they exist.
        try {
            Field innerListField = ExpenseList.class.getDeclaredField("innerList");
            innerListField.setAccessible(true);
            if (Modifier.isStatic(innerListField.getModifiers())) {
                innerListField.set(null, new ArrayList<Expense>());
            }
        } catch (NoSuchFieldException e) {
            // No static innerList found.
        }
        try {
            Field totalExpensesField = ExpenseList.class.getDeclaredField("totalExpenses");
            totalExpensesField.setAccessible(true);
            if (Modifier.isStatic(totalExpensesField.getModifiers())) {
                totalExpensesField.set(null, 0.0);
            }
        } catch (NoSuchFieldException e) {
            // No static totalExpenses found.
        }
    }

    private static FinanceTracker getFinanceTracker5Expenses() {
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        financeTracker.logExpense("log-expense category/food desc/Lunch amt/12.5 d/2025-03-13");
        financeTracker.logExpense("log-expense category/transport desc/Transport  amt/3.2 d/2025-03-12");
        financeTracker.logExpense("log-expense category/food desc/LateLunch amt/13.5 d/2025-03-14");
        financeTracker.logExpense("log-expense category/food desc/Groceries amt/25.0 d/2025-03-11");
        financeTracker.logExpense("log-expense category/food desc/ExpensiveLunch amt/30.0 d/2025-03-15");
        return financeTracker;
    }

    private static FinanceTracker getFinanceTracker3Expenses() {
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        financeTracker.logExpense("log-expense category/food desc/Lunch  amt/12.5 d/2025-03-13");
        financeTracker.logExpense("log-expense category/transport desc/Transport amt/3.2 d/2025-03-12");
        financeTracker.logExpense("log-expense category/food desc/Groceries amt/25.0 d/2025-03-11");
        return financeTracker;
    }

    private static FinanceTracker getFinanceTracker3Income() {
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        financeTracker.addIncome("add category/Part-timeJob amt/300.00 d/June12");
        financeTracker.addIncome("add category/freelance amt/100.00 d/May29");
        financeTracker.addIncome("add category/fulltime-job amt/5000.00 d/Jan1");
        return financeTracker;
    }

    @Test
    void viewAllExpenses_normalTest() {
        FinanceTracker financeTracker = getFinanceTracker3Expenses();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.viewAllExpenses();
        System.setOut(originalOut);
        String expectedOutput = "Expenses log:" + System.lineSeparator()
                + "1 | food | Lunch | $12.50 | 2025-03-13" + System.lineSeparator()
                + "2 | transport | Transport | $3.20 | 2025-03-12" + System.lineSeparator()
                + "3 | food | Groceries | $25.00 | 2025-03-11" + System.lineSeparator()
                + "Total Expenses: $40.70" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void viewAllExpenses_emptyList() {
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.viewAllExpenses();
        System.setOut(originalOut);
        String expectedOutput = "No expenses have been logged yet." + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void findExpense_found1Expense() {
        FinanceTracker financeTracker = getFinanceTracker3Expenses();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.findExpense("find-expense Lunch");
        System.setOut(originalOut);
        String expectedOutput = "Here are all matching expenses: " + System.lineSeparator()
                + "food | Lunch | $12.50 | 2025-03-13" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void findExpense_foundMultipleExpenses() {
        FinanceTracker financeTracker = getFinanceTracker5Expenses();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.findExpense("find-expense Lunch");
        System.setOut(originalOut);
        String expectedOutput = "Here are all matching expenses: " + System.lineSeparator()
                + "food | Lunch | $12.50 | 2025-03-13" + System.lineSeparator()
                + "food | LateLunch | $13.50 | 2025-03-14" + System.lineSeparator()
                + "food | ExpensiveLunch | $30.00 | 2025-03-15" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void findExpense_foundNoMatchingExpense() {
        FinanceTracker financeTracker = getFinanceTracker3Expenses();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.findExpense("find-expense IAmDummy");
        System.setOut(originalOut);
        String expectedOutput = "Sorry, I cannot find any expenses matching your keyword: IAmDummy"
                + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void findExpenses_noKeyword() {
        FinanceTracker financeTracker = getFinanceTracker3Expenses();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.findExpense("find-expense");
        System.setOut(originalOut);
        String expectedOutput = "Error: Missing keyword" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void deleteIncome_validIncome_expectIncomeFound() {
        FinanceTracker financeTracker = getFinanceTracker3Income();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.deleteIncome("Part-timeJob");
        System.setOut(originalOut);
        String expectedOutput = "Income deleted: Part-timeJob";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void deleteIncome_invalidIncome_expectNoIncomeFound() {
        FinanceTracker financeTracker = getFinanceTracker3Income();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.deleteIncome("Housework");
        System.setOut(originalOut);
        String expectedOutput = "Income not found: Housework";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void deleteExpense_validExpense_expectExpenseFound() {
        FinanceTracker financeTracker = getFinanceTracker3Expenses();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.deleteExpense("Lunch");
        System.setOut(originalOut);
        String expectedOutput = "Expense deleted: Lunch";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void deleteExpense_invalidExpense_expectExpenseNotFound() {
        FinanceTracker financeTracker = getFinanceTracker3Expenses();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        financeTracker.deleteExpense("Dinner");
        System.setOut(originalOut);
        String expectedOutput = "Expense not found: Dinner";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void addIncome_validInput_addsIncome() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        assert financeTracker != null : "FinanceTracker instance should not be null";
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.addIncome("add category/Salary amt/2500.00 d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Income added: Salary, Amount: $2500.00, Date: 2025-03-15";
        assertEquals(expectedOutput, outContent.toString().trim());

        // Assert: Output should not be empty
        assert !outContent.toString().trim().isEmpty() : "Expected output should not be empty";
    }

    @Test
    void addIncome_missingCategory_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        assert financeTracker != null : "FinanceTracker instance should not be null";

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - missing category
        financeTracker.addIncome("add amt/2500.00 d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Income category is required.";
        assertEquals(expectedOutput, outContent.toString().trim());

        // Assert: Ensure the error message is not empty
        assert !outContent.toString().trim().isEmpty() : "Error message should not be empty";
    }

    @Test
    void addIncome_missingAmount_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - missing amount
        financeTracker.addIncome("add category/Salary d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Income amount is required.";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void addIncome_missingDate_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - missing date
        financeTracker.addIncome("add category/Salary amt/2500.00");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Income date is required.";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void addIncome_invalidAmountFormat_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - invalid amount format
        financeTracker.addIncome("add category/Salary amt/invalid d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Income amount is required.";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void logExpense_validInput_logsExpense() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.logExpense("log-expense category/Dining desc/DinnerWithFriends amt/45.75 d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Expense logged: Dining | DinnerWithFriends | $45.75 | 2025-03-15";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void logExpense_missingCategory_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - missing category
        financeTracker.logExpense("log-expense desc/DinnerWithFriends amt/45.75 d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Expense category is required.";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void logExpense_missingDescription_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - missing description
        financeTracker.logExpense("log-expense category/Dining amt/45.75 d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Expense description is required.";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void logExpense_missingAmount_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - missing amount
        financeTracker.logExpense("log-expense category/Dining desc/DinnerWithFriends d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Expense amount is required.";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void logExpense_missingDate_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - missing date
        financeTracker.logExpense("log-expense category/Dining desc/DinnerWithFriends amt/45.75");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Expense date is required.";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void logExpense_invalidAmountFormat_showsError() {
        // Create an empty FinanceTracker
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        assert financeTracker != null : "FinanceTracker instance should not be null";

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act - invalid amount format
        financeTracker.logExpense("log-expense category/Dining desc/DinnerWithFriends amt/invalid d/2025-03-15");

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Error: Expense amount is required.";
        assertEquals(expectedOutput, outContent.toString().trim());

        // Assert: Ensure the log is not empty
        assert !outContent.toString().trim().isEmpty() : "Expense log should not be empty";
    }

    @Test
    void addSalaryTest() {
        FinanceTracker tracker = new FinanceTracker(new Scanner(System.in));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        tracker.addIncome("add category/Salary amt/3000.00 d/2025-03-16");
        System.setOut(originalOut);
        String expectedOutput = "Income added: Salary, Amount: $3000.00, Date: 2025-03-16";
        assertEquals(expectedOutput, outContent.toString().trim());
    }

    @Test
    void logExpenseTest() {
        FinanceTracker tracker = new FinanceTracker(new Scanner(System.in));
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        tracker.logExpense("log-expense category/Travel desc/Flight amt/200.00 d/2025-03-20");
        System.setOut(originalOut);
        String expectedOutput = "Expense logged: Travel | Flight | $200.00 | 2025-03-20";
        assertEquals(expectedOutput, outContent.toString().trim());
    }
}
