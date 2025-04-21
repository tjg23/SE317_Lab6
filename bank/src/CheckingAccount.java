import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckingAccount extends BankAccount {
	private static final double DAILY_WITHDRAWAL_LIMIT = 500.0;
	private static final String DB_URL = "jdbc:sqlite:bank.db";

	public CheckingAccount(String accountNumber, double initialBalance) {
		super(accountNumber, initialBalance);
		saveAccount("Checking");
	}

	public CheckingAccount(double initialBalance) {
		super("Checking" + (++accountCounter), initialBalance);
		saveAccount("Checking");
	}

	protected void saveAccount(String accountType) {
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

	private double getDailyWithdrawals() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn
						.prepareStatement("SELECT dailyWithdrawals FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, accountNumber);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getDouble("dailyWithdrawals");
			}
		} catch (SQLException e) {
			System.out.println("Error retrieving daily withdrawals: " + e.getMessage());
		}
		return 0.0;
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

	private void updateBalanceAndDailyWithdrawals(double newBalance, double newDailyWithdrawals) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(
						"UPDATE accounts SET balance = ?, dailyWithdrawals = ? WHERE accountNumber = ?")) {
			pstmt.setDouble(1, newBalance);
			pstmt.setDouble(2, newDailyWithdrawals);
			pstmt.setString(3, accountNumber);
			pstmt.executeUpdate();
			this.balance = newBalance;
		} catch (SQLException e) {
			System.out.println("Error updating balance and withdrawals: " + e.getMessage());
		}
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

	private void updateBalance(double newBalance) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(
						"UPDATE accounts SET balance = ? WHERE accountNumber = ?")) {
			pstmt.setDouble(1, newBalance);
			pstmt.setString(2, accountNumber);
			pstmt.executeUpdate();
			this.balance = newBalance;
		} catch (SQLException e) {
			System.out.println("Error updating balance: " + e.getMessage());
		}
	}

	public String getAccountNumber() {
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

	public void withdraw(double amount) throws Exception {
		if (amount <= 0) {
			System.out.println("Withdrawal amount must be positive.");
			return;
		}
		double currentBalance = getBalance();
		double currentDailyWithdrawals = getDailyWithdrawals();
		if (amount > currentBalance) {
			throw new Exception("Insufficient funds. Cannot withdraw " + amount);
		} else if (currentDailyWithdrawals + amount > DAILY_WITHDRAWAL_LIMIT) {
			throw new Exception("Daily withdrawal limit exceeded. Cannot withdraw " + amount);
		} else {
			updateBalanceAndDailyWithdrawals(currentBalance - amount, currentDailyWithdrawals + amount);
		}
	}

	public void transfer(double amount, BankAccount savingAccount) throws Exception {
		if (amount <= 0) {
			throw new Exception("Transfer amount must be positive.");
		}
		double currentBalance = getBalance();
		if (amount > currentBalance) {
			throw new Exception("Insufficient funds. Cannot transfer " + amount);
		} else {
			updateBalance(currentBalance - amount);
			savingAccount.deposit(amount);
		}
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
