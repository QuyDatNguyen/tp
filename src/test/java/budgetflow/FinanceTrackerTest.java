package budgetflow;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinanceTrackerTest {
    public static final String EMPTY_EXPENSE_LIST_MESSAGE =
            "There is currently no expense in your list right now. Please add more expenses to continue";

    @Test
    void viewAllExpenses_normalTest() {
        // Create FinanceTracker with predefined expenses
        FinanceTracker financeTracker = getFinanceTracker3Expenses();

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.viewAllExpenses();

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Expenses log: " + System.lineSeparator() +
                "Lunch | $12.50 | 2025-03-13" + System.lineSeparator() +
                "Transport | $3.20 | 2025-03-12" + System.lineSeparator() +
                "Groceries | $25.00 | 2025-03-11" + System.lineSeparator() + System.lineSeparator() +
                "Total Expenses: $40.70" + System.lineSeparator();

        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void viewAllExpenses_emptyList() {
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.viewAllExpenses();

        // Reset System.out
        System.setOut(originalOut);

        // Assert
        String expectedOutput = "Expenses log: " + System.lineSeparator() +
                EMPTY_EXPENSE_LIST_MESSAGE + System.lineSeparator() +
                "Total Expenses: $0.00" + System.lineSeparator();

        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void findExpense_found1Expense() {
        // Create FinanceTracker with predefined expenses
        FinanceTracker financeTracker = getFinanceTracker3Expenses();

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.findExpense("find-expense Lunch");

        // Reset System.out
        System.setOut(originalOut);
        // Assert
        String expectedOutput = "Here are all matching expenses: " + System.lineSeparator() +
                "Lunch | $12.50 | 2025-03-13" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    private static FinanceTracker getFinanceTracker3Expenses() {
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        financeTracker.logExpense("log-expense desc/Lunch amt/12.5 d/2025-03-13");
        financeTracker.logExpense("log-expense desc/Transport amt/3.2 d/2025-03-12");
        financeTracker.logExpense("log-expense desc/Groceries amt/25.0 d/2025-03-11");
        return financeTracker;
    }

    @Test
    void findExpense_foundMultipleExpenses() {
        // Create FinanceTracker with predefined expenses
        FinanceTracker financeTracker = getFinanceTracker5Expenses();

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.findExpense("find-expense Lunch");

        // Reset System.out
        System.setOut(originalOut);
        // Assert
        String expectedOutput = "Here are all matching expenses: " + System.lineSeparator() +
                "Lunch | $12.50 | 2025-03-13" + System.lineSeparator() +
                "Late Lunch | $13.50 | 2025-03-14" + System.lineSeparator() +
                "Expensive Lunch | $30.00 | 2025-03-15" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    private static FinanceTracker getFinanceTracker5Expenses() {
        FinanceTracker financeTracker = new FinanceTracker(new Scanner(System.in));
        financeTracker.logExpense("log-expense desc/Lunch amt/12.5 d/2025-03-13");
        financeTracker.logExpense("log-expense desc/Transport amt/3.2 d/2025-03-12");
        financeTracker.logExpense("log-expense desc/Late Lunch amt/13.5 d/2025-03-14");
        financeTracker.logExpense("log-expense desc/Groceries amt/25.0 d/2025-03-11");
        financeTracker.logExpense("log-expense desc/Expensive Lunch amt/30.0 d/2025-03-15");
        return financeTracker;
    }

    @Test
    void findExpense_foundNoMatchingExpense() {
        // Create FinanceTracker with predefined expenses
        FinanceTracker financeTracker = getFinanceTracker3Expenses();

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.findExpense("find-expense IAmDummy");

        // Reset System.out
        System.setOut(originalOut);
        // Assert
        String expectedOutput = "Sorry, I cannot find any expenses matching your keyword: IAmDummy" +
                System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    void findExpenses_noKeyword() {
        // Create FinanceTracker with predefined expenses
        FinanceTracker financeTracker = getFinanceTracker3Expenses();

        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Act
        financeTracker.findExpense("find-expense");
        // Reset System.out
        System.setOut(originalOut);
        // Assert
        String expectedOutput = "Error: Missing keyword" +
                System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }

}
