import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public abstract class BankAccount {
	protected static final String DB_URL = "jdbc:sqlite:bank.db";

	protected static Integer accountCounter = 0;
	protected String accountNumber;
	protected double balance;

	protected double dailyDeposits;
	protected static final double DAILY_DEPOSIT_LIMIT = 5000.0;
	protected Date transactionDate;

	public BankAccount(String accountNumber, double initialBalance) {
		this.accountNumber = accountNumber;
		this.balance = initialBalance;
	}

	public BankAccount(double initialBalance) {
		this.accountNumber = "Account" + (++accountCounter);
		this.balance = initialBalance;
	}

	abstract public void transfer(double amount, BankAccount targetAccount) throws Exception;

	public void saveAccount(String accountType) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(
						"INSERT OR REPLACE INTO accounts (accountNumber, balance, dailyWithdrawals, dailyTransfers, dailyDeposits, accountType) "
								+
								"VALUES (?, ?, ?, ?, ?, ?)")) {
			pstmt.setString(1, accountNumber);
			pstmt.setDouble(2, balance);
			pstmt.setDouble(3, 0.0);
			pstmt.setDouble(4, 0.0);
			pstmt.setDouble(5, 0.0);
			pstmt.setString(6, accountType);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error saving account: " + e.getMessage());
		}
	}

	protected double getDailyDeposits() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT dailyDeposits FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, accountNumber);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getDouble("dailyDeposits");
			}
		} catch (SQLException e) {
			System.out.println("Error retrieving daily deposits: " + e.getMessage());
		}
		return 0.0;
	}

	protected void updateBalanceAndDailyDeposits(double newBalance, double newDailyDeposits) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(
						"UPDATE accounts SET balance = ?, dailyDeposits = ? WHERE accountNumber = ?")) {
			pstmt.setDouble(1, newBalance);
			pstmt.setDouble(2, newDailyDeposits);
			pstmt.setString(3, accountNumber);
			pstmt.executeUpdate();
			this.balance = newBalance;
		} catch (SQLException e) {
			System.out.println("Error updating balance and deposits: " + e.getMessage());
		}
	}

	protected String getAccountNumber() {
		return accountNumber;
	}

	public double getBalance() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement("SELECT balance FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, accountNumber);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				this.balance = rs.getDouble("balance");
				return this.balance;
			}
		} catch (SQLException e) {
			System.out.println("Error retrieving balance: " + e.getMessage());
		}
		return this.balance;
	}

	public void deposit(double amount) throws Exception {
		if (amount <= 0) {
			throw new Exception("Deposit amount must be positive.");
		}
		double currentBalance = getBalance();
		double currentDailyDeposits = getDailyDeposits();
		if (currentDailyDeposits + amount > DAILY_DEPOSIT_LIMIT) {
			throw new Exception("Daily deposit limit exceeded. Cannot deposit " + amount);
		} else {
			updateBalanceAndDailyDeposits(currentBalance + amount, currentDailyDeposits + amount);
		}
	}

	public void resetDailyLimits() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(
						"UPDATE accounts SET dailyDeposits = 0, dailyWithdrawals = 0, dailyTransfers = 0 WHERE accountNumber = ?")) {
			pstmt.setString(1, accountNumber);
			pstmt.executeUpdate();
			this.dailyDeposits = 0;
		} catch (SQLException e) {
			System.out.println("Error resetting daily limits: " + e.getMessage());
		}
	}
}
