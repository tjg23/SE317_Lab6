import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class User {
	protected static final String DB_URL = "jdbc:sqlite:bank.db";

	private String name;
	private String pin;

	private CheckingAccount checkingAccount;
	private SavingAccount savingAccount;

	public User(String name, String pin, CheckingAccount checkingAccount,
			SavingAccount savingAccount) {
		this.name = name;
		this.pin = pin;
		this.checkingAccount = checkingAccount;
		this.savingAccount = savingAccount;
		// saveAccount();
	}

	public void saveUser() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(
						"INSERT OR REPLACE INTO users (name, pin, checkingAccount, savingAccount) "
								+ "VALUES (?, ?, ?, ?)")) {
			pstmt.setString(1, name);
			pstmt.setString(2, pin);
			pstmt.setString(3, checkingAccount.getAccountNumber());
			pstmt.setString(4, savingAccount.getAccountNumber());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error saving user: " + e.getMessage());
		}
	}

	public CheckingAccount getCheckingAccount() {
		return checkingAccount;
	}

	public SavingAccount getSavingAccount() {
		return savingAccount;
	}

	public String getName() {
		return name;
	}

	public String getPin() {
		return pin;
	}

}
