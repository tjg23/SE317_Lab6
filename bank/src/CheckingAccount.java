import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckingAccount extends BankAccount {
	private static final double DAILY_WITHDRAWAL_LIMIT = 500.0;

	public CheckingAccount(String accountNumber, double initialBalance) {
		super(accountNumber, initialBalance);
		// saveAccount("Checking");
	}

	public CheckingAccount(double initialBalance) {
		super("Checking" + (++accountCounter), initialBalance);
		// saveAccount("Checking");
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

}
