public class SavingAccount extends BankAccount {
	private double dailyTransfers;
	private static final double DAILY_TRANSFER_LIMIT = 100.0;

	public SavingAccount(String accountNumber, double initialBalance) {
		super(accountNumber, initialBalance);
	}

	public SavingAccount(double initialBalance) {
		super("Saving" + (++accountCounter), initialBalance);
	}

	public void transfer(double amount, CheckingAccount checkingAccount) {
		if (amount <= 0) {
			System.out.println("Transfer amount must be positive.");
		} else if (amount > balance) {
			System.out.println("Insufficient funds. Cannot transfer " + amount);
		} else if (dailyTransfers + amount > DAILY_TRANSFER_LIMIT) {
			System.out.println("Daily transfer limit exceeded. Cannot transfer " + amount);
		} else {
			balance -= amount;
			dailyTransfers += amount;
			checkingAccount.deposit(amount);
			System.out.println("Transferred " + amount + " from Saving to Checking account.");
		}
	}

	public void resetDailyLimits() {
		dailyDeposits = 0;
		dailyTransfers = 0;
	}
}
