package bank;

import java.util.Date;

public class BankAccount {
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

	public String getAccountNumber() {
		return accountNumber;
	}

	public double getBalance() {
		return balance;
	}

	public void deposit(double amount) {
		if (amount <= 0) {
			System.out.println("Deposit amount must be positive.");
		} else if (dailyDeposits + amount > DAILY_DEPOSIT_LIMIT) {
			System.out.println("Daily deposit limit exceeded. Cannot deposit " + amount);
		} else {
			balance += amount;
			dailyDeposits += amount;
		}
	}

}
