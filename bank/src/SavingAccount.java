import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SavingAccount extends BankAccount {
	private static final double DAILY_TRANSFER_LIMIT = 100.0;
	private static final String DB_URL = "jdbc:sqlite:bank.db";

	public SavingAccount(String accountNumber, double initialBalance) {
		super(accountNumber, initialBalance);
		initializeDatabase();
		saveAccount("Saving");
	}

	public SavingAccount(double initialBalance) {
		super("Saving" + (++accountCounter), initialBalance);
		initializeDatabase();
		saveAccount("Saving");
	}

	private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "accountNumber TEXT PRIMARY KEY, " +
                    "balance REAL, " +
                    "dailyWithdrawals REAL, " +
                    "dailyTransfers REAL, " +
                    "dailyDeposits REAL, " +
                    "accountType TEXT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Database initialization error: " + e.getMessage());
        }
    }

	protected void saveAccount(String accountType) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO accounts (accountNumber, balance, dailyWithdrawals, dailyTransfers, dailyDeposits, accountType) " +
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

	private double getDailyTransfers() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT dailyTransfers FROM accounts WHERE accountNumber = ?")) {
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
