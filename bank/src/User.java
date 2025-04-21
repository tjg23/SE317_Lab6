import java.util.ArrayList;
import java.util.List;

public class User {
	private static List<User> users = new ArrayList<>();
	private String name;
	private String pin;

	private CheckingAccount checkingAccount;
	private SavingAccount savingAccount;

	public User(String name, String pin, String checkingAccountNumber, double initialCheckingBalance,
			String savingAccountNumber, double initialSavingBalance) {
		this.name = name;
		this.pin = pin;
		this.checkingAccount = new CheckingAccount(checkingAccountNumber, initialCheckingBalance);
		this.savingAccount = new SavingAccount(savingAccountNumber, initialSavingBalance);
		users.add(this);
	}

	public User(String name, String pin, double initialCheckingBalance, double initialSavingBalance) {
		this.name = name;
		this.pin = pin;
		this.checkingAccount = new CheckingAccount(initialCheckingBalance);
		this.savingAccount = new SavingAccount(initialSavingBalance);
		users.add(this);
	}

	public static User logIn(String name, String pin) {
		for (User user : users) {
			if (user.name.equals(name) && user.pin.equals(pin)) {
				System.out.println("Logged in successfully.");
				return user;
			}
		}
		System.out.println("Incorrect PIN.");
		return null;
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
