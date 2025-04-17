package utility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UtilityAccount {
	private static List<UtilityAccount> accounts = new ArrayList<>();

	private static int accountCounter = 0;
	private String accountNumber;

	private String username;
	private String password;

	private List<Bill> bills;

	public UtilityAccount(String username, String password) {
		this.username = username;
		this.password = password;
		this.accountNumber = String.format("%06d", ++accountCounter);
		this.bills = new ArrayList<>();
		accounts.add(this);
	}

	public static UtilityAccount logIn(String nameOrNumber, String password) {
		for (UtilityAccount account : accounts) {
			if ((account.username.equals(nameOrNumber) || account.accountNumber.equals(nameOrNumber))
					&& account.password.equals(password)) {
				System.out.println("Logged in successfully.");
				return account;
			}
		}
		System.out.println("Invalid username or password.");
		return null;
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
