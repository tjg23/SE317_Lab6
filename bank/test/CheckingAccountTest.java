import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckingAccountTest {

	private static final String DB_URL = "jdbc:sqlite:bank.db";

	@Before
	public void setUp() throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("DELETE FROM accounts WHERE accountNumber LIKE 'CHK_TEST_%'")) {
			pstmt.executeUpdate();
		}
	}

	@Test
	public void testCreateCheckingAccount() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_1", 100.0);
		account.saveAccount("Checking");
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, "CHK_TEST_1");
			ResultSet rs = pstmt.executeQuery();
			assertTrue("Account should exist in database", rs.next());
			assertEquals("CHK_TEST_1", rs.getString("accountNumber"));
			assertEquals(100.0, rs.getDouble("balance"), 0.01);
			assertEquals(0.0, rs.getDouble("dailyWithdrawals"), 0.01);
			assertEquals(0.0, rs.getDouble("dailyDeposits"), 0.01);
			assertEquals("Checking", rs.getString("accountType"));
		}
	}

	@Test
	public void testDepositValidAmount() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_2", 100.0);
		account.saveAccount("Checking");
		account.deposit(50.0);
		assertEquals("Balance should increase by deposit amount", 150.0, account.getBalance(), 0.01);
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT dailyDeposits FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, "CHK_TEST_2");
			ResultSet rs = pstmt.executeQuery();
			assertTrue(rs.next());
			assertEquals("Daily deposits should reflect the deposit", 50.0, rs.getDouble("dailyDeposits"), 0.01);
		}
	}

	@Test
	public void testDepositNegativeAmount() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_3", 100.0);
		account.saveAccount("Checking");
		try {
			account.deposit(-50.0);
			fail("Should throw exception for negative deposit");
		} catch (Exception e) {
			assertEquals("Deposit amount must be positive.", e.getMessage());
			assertEquals("Balance should remain unchanged", 100.0, account.getBalance(), 0.01);
		}
	}

	@Test
	public void testDepositExceedingDailyLimit() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_4", 100.0);
		account.saveAccount("Checking");
		// Set dailyDeposits near the limit (5000.0)
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn
						.prepareStatement("UPDATE accounts SET dailyDeposits = 4950.0 WHERE accountNumber = ?")) {
			pstmt.setString(1, "CHK_TEST_4");
			pstmt.executeUpdate();
		}
		try {
			account.deposit(100.0); // 4950 + 100 > 5000
			fail("Should throw exception for exceeding daily limit");
		} catch (Exception e) {
			assertEquals("Daily deposit limit exceeded. Cannot deposit 100.0", e.getMessage());
			assertEquals("Balance should remain unchanged", 100.0, account.getBalance(), 0.01);
		}
	}

	@Test
	public void testWithdrawValidAmount() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_5", 100.0);
		account.saveAccount("Checking");
		account.withdraw(50.0);
		assertEquals("Balance should decrease by withdrawal amount", 50.0, account.getBalance(), 0.01);
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn
						.prepareStatement("SELECT dailyWithdrawals FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, "CHK_TEST_5");
			ResultSet rs = pstmt.executeQuery();
			assertTrue(rs.next());
			assertEquals("Daily withdrawals should reflect the withdrawal", 50.0, rs.getDouble("dailyWithdrawals"), 0.01);
		}
	}

	@Test
	public void testWithdrawExceedingBalance() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_6", 100.0);
		account.saveAccount("Checking");
		try {
			account.withdraw(150.0);
			fail("Should throw exception for insufficient funds");
		} catch (Exception e) {
			assertEquals("Insufficient funds. Cannot withdraw 150.0", e.getMessage());
			assertEquals("Balance should remain unchanged", 100.0, account.getBalance(), 0.01);
		}
	}

	@Test
	public void testTransferValidAmount() throws Exception {
		CheckingAccount checking = new CheckingAccount("CHK_TEST_7", 100.0);
		checking.saveAccount("Checking");
		SavingAccount saving = new SavingAccount("SAV_TEST7", 50.0);
		saving.saveAccount("Saving");
		checking.transfer(30.0, saving);
		assertEquals("Checking balance should decrease", 70.0, checking.getBalance(), 0.01);
		assertEquals("Savings balance should increase", 80.0, saving.getBalance(), 0.01);
	}

	@Test
	public void testNullBalanceInDatabase() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_8", 100.0);
		account.saveAccount("Checking");
		// Set balance to null in database
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("UPDATE accounts SET balance = NULL WHERE accountNumber = ?")) {
			pstmt.setString(1, "CHK_TEST_8");
			pstmt.executeUpdate();
		}
		double balance = account.getBalance();
		assertEquals("Should return 0.0 for null balance per SQLite behavior", 0.0, balance, 0.01);
	}

	@Test
	public void testEmptyElementZeroBalance() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_9", 0.0);
		account.saveAccount("Checking");
		assertEquals("Balance should be zero", 0.0, account.getBalance(), 0.01);
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT balance FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, "CHK_TEST_9");
			ResultSet rs = pstmt.executeQuery();
			assertTrue(rs.next());
			assertEquals(0.0, rs.getDouble("balance"), 0.01);
		}
	}

	@Test
	public void testNormalCaseSingleElement() throws Exception {
		CheckingAccount account = new CheckingAccount("CHK_TEST_10", 200.0);
		account.saveAccount("Checking");
		assertEquals("Balance should match initial value", 200.0, account.getBalance(), 0.01);
	}
}