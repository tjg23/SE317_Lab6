import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SavingAccountTest {

	private static final String DB_URL = "jdbc:sqlite:bank.db";

	@Before
	public void setUp() throws SQLException {
		// Clean the database before each test to ensure isolation
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("DELETE FROM accounts WHERE accountNumber LIKE 'SAV_TEST_%'")) {
			pstmt.executeUpdate();
		}
	}

	@Test
	public void testCreateSavingAccount() throws Exception {
		SavingAccount account = new SavingAccount("SAV_TEST_1", 100.0);
		account.saveAccount("Saving");
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, "SAV_TEST_1");
			ResultSet rs = pstmt.executeQuery();
			assertTrue("Account should exist in database", rs.next());
			assertEquals("SAV_TEST_1", rs.getString("accountNumber"));
			assertEquals(100.0, rs.getDouble("balance"), 0.01);
			assertEquals(0.0, rs.getDouble("dailyTransfers"), 0.01);
			assertEquals(0.0, rs.getDouble("dailyDeposits"), 0.01);
			assertEquals("Saving", rs.getString("accountType"));
		}
	}

	@Test
	public void testDepositValidAmount() throws Exception {
		SavingAccount account = new SavingAccount("SAV_TEST_2", 100.0);
		account.saveAccount("Saving");
		account.deposit(50.0);
		assertEquals("Balance should increase by deposit amount", 150.0, account.getBalance(), 0.01);
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT dailyDeposits FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, "SAV_TEST_2");
			ResultSet rs = pstmt.executeQuery();
			assertTrue(rs.next());
			assertEquals("Daily deposits should reflect the deposit", 50.0, rs.getDouble("dailyDeposits"), 0.01);
		}
	}

	@Test
	public void testDepositNegativeAmount() throws Exception {
		SavingAccount account = new SavingAccount("SAV_TEST_3", 100.0);
		account.saveAccount("Saving");
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
		SavingAccount account = new SavingAccount("SAV_TEST_4", 100.0);
		account.saveAccount("Saving");
		// Set dailyDeposits near the limit (5000.0)
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn
						.prepareStatement("UPDATE accounts SET dailyDeposits = 4950.0 WHERE accountNumber = ?")) {
			pstmt.setString(1, "SAV_TEST_4");
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
	public void testTransferValidAmount() throws Exception {
		SavingAccount saving = new SavingAccount("SAV_TEST_5", 100.0);
		saving.saveAccount("Saving");
		CheckingAccount checking = new CheckingAccount("TEST_CHK5", 50.0);
		checking.saveAccount("Checking");
		saving.transfer(30.0, checking);
		assertEquals("Saving balance should decrease", 70.0, saving.getBalance(), 0.01);
		assertEquals("Checking balance should increase", 80.0, checking.getBalance(), 0.01);
	}

	@Test
	public void testTransferExceedingBalance() throws Exception {
		SavingAccount saving = new SavingAccount("SAV_TEST_6", 100.0);
		saving.saveAccount("Saving");
		CheckingAccount checking = new CheckingAccount("TEST_CHK6", 50.0);
		checking.saveAccount("Checking");
		try {
			saving.transfer(150.0, checking);
			fail("Should throw exception for insufficient funds");
		} catch (Exception e) {
			assertEquals("Insufficient funds. Cannot transfer 150.0", e.getMessage());
			assertEquals("Saving balance should remain unchanged", 100.0, saving.getBalance(), 0.01);
			assertEquals("Checking balance should remain unchanged", 50.0, checking.getBalance(), 0.01);
		}
	}

	@Test
	public void testTransferExceedingDailyLimit() throws Exception {
		SavingAccount saving = new SavingAccount("SAV_TEST_7", 100.0);
		saving.saveAccount("Saving");
		CheckingAccount checking = new CheckingAccount("TEST_CHK7", 50.0);
		checking.saveAccount("Checking");
		// Set dailyTransfers near the limit (100.0)
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn
						.prepareStatement("UPDATE accounts SET dailyTransfers = 90.0 WHERE accountNumber = ?")) {
			pstmt.setString(1, "SAV_TEST_7");
			pstmt.executeUpdate();
		}
		try {
			saving.transfer(20.0, checking); // 90 + 20 > 100
			fail("Should throw exception for exceeding daily transfer limit");
		} catch (Exception e) {
			assertEquals("Daily transfer limit exceeded. Cannot transfer 20.0", e.getMessage());
			assertEquals("Saving balance should remain unchanged", 100.0, saving.getBalance(), 0.01);
			assertEquals("Checking balance should remain unchanged", 50.0, checking.getBalance(), 0.01);
		}
	}

	@Test
	public void testNullBalanceInDatabase() throws Exception {
		SavingAccount account = new SavingAccount("SAV_TEST_8", 100.0);
		account.saveAccount("Saving");
		// Set balance to null in database
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("UPDATE accounts SET balance = NULL WHERE accountNumber = ?")) {
			pstmt.setString(1, "SAV_TEST_8");
			pstmt.executeUpdate();
		}
		double balance = account.getBalance();
		assertEquals("Should return 0.0 for null balance per SQLite behavior", 0.0, balance, 0.01);
	}

	@Test
	public void testEmptyElementZeroBalance() throws Exception {
		SavingAccount account = new SavingAccount("SAV_TEST_9", 0.0);
		account.saveAccount("Saving");
		assertEquals("Balance should be zero", 0.0, account.getBalance(), 0.01);
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT balance FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, "SAV_TEST_9");
			ResultSet rs = pstmt.executeQuery();
			assertTrue(rs.next());
			assertEquals(0.0, rs.getDouble("balance"), 0.01);
		}
	}

	@Test
	public void testNormalCaseSingleElement() throws Exception {
		SavingAccount account = new SavingAccount("SAV_TEST_10", 200.0);
		account.saveAccount("Saving");
		assertEquals("Balance should match initial value", 200.0, account.getBalance(), 0.01);
	}
}