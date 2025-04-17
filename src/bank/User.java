package bank;

import java.util.ArrayList;
import java.util.List;

import utility.UtilityAccount;

public class User {
	private static List<User> users = new ArrayList<>();
	private String pin;

	private CheckingAccount checkingAccount;
	private SavingAccount savingAccount;

	private UtilityAccount utilityAccount;

	public User(String pin, String checkingAccountNumber, double initialCheckingBalance,
			String savingAccountNumber, double initialSavingBalance) {
		this.pin = pin;
		this.checkingAccount = new CheckingAccount(checkingAccountNumber, initialCheckingBalance);
		this.savingAccount = new SavingAccount(savingAccountNumber, initialSavingBalance);
		users.add(this);
	}

	public User(String pin, double initialCheckingBalance, double initialSavingBalance) {
		this.pin = pin;
		this.checkingAccount = new CheckingAccount(initialCheckingBalance);
		this.savingAccount = new SavingAccount(initialSavingBalance);
		users.add(this);
	}

	public static User logIn(String pin) {
		for (User user : users) {
			if (user.pin.equals(pin)) {
				System.out.println("Logged in successfully.");
				return user;
			}
		}
		System.out.println("Invalid PIN.");
		return null;
	}

	public void logInUtility(String nameOrNumber, String password) {
		this.utilityAccount = UtilityAccount.logIn(nameOrNumber, password);
	}

	public CheckingAccount getCheckingAccount() {
		return checkingAccount;
	}

	public SavingAccount getSavingAccount() {
		return savingAccount;
	}

	public UtilityAccount getUtilityAccount() {
		return utilityAccount;
	}

	public String getPin() {
		return pin;
	}

}
