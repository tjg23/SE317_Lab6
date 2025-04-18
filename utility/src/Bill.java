import java.time.LocalDate;

public class Bill {
    private int billId;
    private double amount;
    private LocalDate dueDate;
    private LocalDate paidDate;

    public Bill(int billId, double amount, LocalDate dueDate, LocalDate paidDate) {
        this.billId = billId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paidDate = paidDate;
    }

    public int getBillId() {
        return billId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }
}