public class Bill {
	private static int billCounter = 0;
	private String billNumber;
	private double amountDue;
	private String dueDate;

	private String paidDate;

	public Bill(String billNumber, double amountDue, String dueDate) {
		this.billNumber = billNumber;
		this.amountDue = amountDue;
		this.dueDate = dueDate;
	}

	public Bill(double amountDue, String dueDate) {
		this.billNumber = "Bill" + (++billCounter);
		this.amountDue = amountDue;
		this.dueDate = dueDate;
	}

	public String getBillNumber() {
		return billNumber;
	}

	public double getAmountDue() {
		return amountDue;
	}

	public String getDueDate() {
		return dueDate;
	}

	public String getPaidDate() {
		return paidDate;
	}

	public void payBill(String paidDate) {
		this.paidDate = paidDate;
		System.out.println("Bill " + billNumber + " paid on " + paidDate);
	}

}
