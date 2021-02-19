import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class BankAccount {

    public int serialNumber;
    public int initialAccountBalance;
    public int currentAccountBalance;
    public List<Transactions> logsTransactions;
    public static int id;

    public ReentrantLock mutex = new ReentrantLock();
    public static int amount=1500;

    public BankAccount()
    {
        this.id++;
        this.serialNumber = id;
        initialAccountBalance=amount;
        currentAccountBalance=amount;
        this.logsTransactions = new ArrayList<Transactions>();
    }

    public String toString()
    {
        String transactions = "Transaction id: "+ this.serialNumber + ", Initial Amount Balance: "+ this.initialAccountBalance + ", Current Amount Balance: " + currentAccountBalance+", ";
        transactions += "Log of Transactions: ";
        logsTransactions.stream()
                .map(t -> t.toString())
                .collect(Collectors.joining(", "));
        return transactions;
    }

    public boolean checkIfValid()
    {
        int initialAmount = this.initialAccountBalance;
        for (Transactions t: logsTransactions)
        {
            if(!t.transactionType)
            {
                initialAmount -= t.amount;
            }
            else
            {
                initialAmount += t.amount;
            }

        }
        if(initialAmount == currentAccountBalance)
        {
            return true;
        }
        return false;
    }

    public void addLogTransaction(Transactions t)
    {
        logsTransactions.add(t);
    }


}
