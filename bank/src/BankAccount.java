import java.util.Date;

public abstract class BankAccount {
	protected static Integer accountCounter = 0;
	protected String accountNumber;
	protected double balance;

	protected double dailyDeposits;
	protected static final double DAILY_DEPOSIT_LIMIT = 5000.0;
	protected Date transactionDate;

	public BankAccount(String accountNumber, double initialBalance) {
		this.accountNumber = accountNumber;
		this.balance = initialBalance;
	}

	public BankAccount(double initialBalance) {
		this.accountNumber = "Account" + (++accountCounter);
		this.balance = initialBalance;
	}

	abstract protected void saveAccount(String accountType);

	abstract protected double getDailyDeposits();

	abstract protected void updateBalanceAndDailyDeposits(double amount, double newDailyDeposits);

	abstract protected String getAccountNumber();

	abstract public double getBalance();

	abstract public void transfer(double amount, BankAccount targetAccount) throws Exception;

	abstract public void deposit(double amount) throws Exception;

	abstract public void resetDailyLimits();
}
