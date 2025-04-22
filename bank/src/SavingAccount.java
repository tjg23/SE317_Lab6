import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SavingAccount extends BankAccount {
	private static final double DAILY_TRANSFER_LIMIT = 100.0;

	public SavingAccount(String accountNumber, double initialBalance) {
		super(accountNumber, initialBalance);
		saveAccount("Saving");
	}

	public SavingAccount(double initialBalance) {
		super("Saving" + (++accountCounter), initialBalance);
		saveAccount("Saving");
	}

	private double getDailyTransfers() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn
						.prepareStatement("SELECT dailyTransfers FROM accounts WHERE accountNumber = ?")) {
			pstmt.setString(1, accountNumber);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getDouble("dailyTransfers");
			}
		} catch (SQLException e) {
			System.out.println("Error retrieving daily transfers: " + e.getMessage());
		}
		return 0.0;
	}

	private void updateBalanceAndDailyTransfers(double newBalance, double newDailyTransfers) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(
						"UPDATE accounts SET balance = ?, dailyTransfers = ? WHERE accountNumber = ?")) {
			pstmt.setDouble(1, newBalance);
			pstmt.setDouble(2, newDailyTransfers);
			pstmt.setString(3, accountNumber);
			pstmt.executeUpdate();
			this.balance = newBalance;
		} catch (SQLException e) {
			System.out.println("Error updating balance and transfers: " + e.getMessage());
		}
	}

	public void transfer(double amount, BankAccount checkingAccount) throws Exception {
		if (amount <= 0) {
			System.out.println("Transfer amount must be positive.");
			return;
		}
		double currentBalance = getBalance();
		double currentDailyTransfers = getDailyTransfers();
		if (amount > currentBalance) {
			throw new Exception("Insufficient funds. Cannot transfer " + amount);
		} else if (currentDailyTransfers + amount > DAILY_TRANSFER_LIMIT) {
			throw new Exception("Daily transfer limit exceeded. Cannot transfer " + amount);
		} else {
			updateBalanceAndDailyTransfers(currentBalance - amount, currentDailyTransfers + amount);
			checkingAccount.deposit(amount);
		}
	}

}
