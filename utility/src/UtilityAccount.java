import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UtilityAccount {
	private String accountNumber;
	private String username;
	private String password;
	private List<Bill> bills;

	private static final String DB_URL = "jdbc:sqlite:utility.db";

	public UtilityAccount(String username, String password) throws SQLException {
		this.username = username;
		this.password = password;
		this.accountNumber = generateAccountNumber();
		this.bills = new ArrayList<>();
		saveToDatabase();
	}

	private UtilityAccount(String accountNumber, String username, String password) {
        this.accountNumber = accountNumber;
        this.username = username;
        this.password = password;
        this.bills = new ArrayList<>();
    }

	private String generateAccountNumber() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM accounts");
            int count = rs.getInt("count") + 1;
            return String.format("%06d", count);
        }
    }

	private void saveToDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO accounts (account_number, username, password) VALUES (?, ?, ?)")) {
            pstmt.setString(1, accountNumber);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
        }
    }

	public static UtilityAccount logIn(String nameOrNumber, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM accounts WHERE (username = ? OR account_number = ?) AND password = ?")) {
            pstmt.setString(1, nameOrNumber);
            pstmt.setString(2, nameOrNumber);
            pstmt.setString(3, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Logged in successfully.");
                UtilityAccount account = new UtilityAccount(
                        rs.getString("account_number"),
                        rs.getString("username"),
                        rs.getString("password")
                );
                account.loadBills(); // Load bills from database
                return account;
            } else {
                System.out.println("Invalid username or password.");
                return null;
            }
        }
    }

	private void loadBills() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM bills WHERE account_number = ?")) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();

            bills.clear();
            while (rs.next()) {
                Bill bill = new Bill(
                        rs.getInt("bill_id"),
                        rs.getDouble("amount"),
                        LocalDate.parse(rs.getString("due_date")),
                        rs.getString("paid_date") != null ? LocalDate.parse(rs.getString("paid_date")) : null
                );
                bills.add(bill);
            }
        }
    }

	public void logOut() {
		System.out.println("Logged out successfully.");
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public String getUsername() {
		return username;
	}

	protected String getPassword() {
		return password;
	}

	public List<Bill> getBills() {
		return bills;
	}

	public List<Bill> getPaidBills() {
		return bills.stream()
				.filter(bill -> bill.getPaidDate() != null)
				.sorted(Comparator.comparing(Bill::getPaidDate))
				.toList();
	}

	public List<Bill> getUnpaidBills() {
		return bills.stream()
				.filter(bill -> bill.getPaidDate() == null)
				.sorted(Comparator.comparing(Bill::getDueDate))
				.toList();
	}

	public void addBill(Bill bill) {
		if (bills == null) {
			bills = new ArrayList<>();
		}
		bills.add(bill);
	}

	private void saveBillToDatabase(Bill bill) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO bills (account_number, amount, due_date, paid_date) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, accountNumber);
            pstmt.setDouble(2, bill.getAmount());
            pstmt.setString(3, bill.getDueDate().toString());
            pstmt.setString(4, bill.getPaidDate() != null ? bill.getPaidDate().toString() : null);
            pstmt.executeUpdate();
        }
    }

	public Bill getNextBill() {
		if (bills == null || bills.isEmpty()) {
			System.out.println("No bills available.");
			return null;
		}
		return bills.stream()
				.filter(bill -> bill.getPaidDate() == null)
				.sorted(Comparator.comparing(Bill::getDueDate))
				.findFirst().orElse(null);
	}

}
