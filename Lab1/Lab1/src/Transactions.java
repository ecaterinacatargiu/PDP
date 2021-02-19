import java.lang.reflect.AccessibleObject;

public class Transactions {

    static int transactionNumber = 0;
    public int transactionId;
    public BankAccount firstAccount;
    public BankAccount secondAccount;
    public int amount;
    public boolean transactionType; //false=>sends money; true=receives money

    public Transactions(int id, BankAccount firstAccount, BankAccount secondAccount, int amount, boolean transactionType)
    {
        this.transactionId = id;
        this.firstAccount = firstAccount;
        this.secondAccount = secondAccount;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public Transactions(BankAccount first, BankAccount second, int amountt, boolean type)
    {
        this.transactionId = transactionNumber;
        transactionNumber++;
        this.firstAccount = first;
        this.secondAccount = second;
        this.amount = amountt;
        this.transactionType = type;
        makeTransaction();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public BankAccount getFirstAccount() {
        return firstAccount;
    }

    public void setFirstAccount(BankAccount firstAccount) {
        this.firstAccount = firstAccount;
    }

    public BankAccount getSecondAccount() {
        return secondAccount;
    }

    public void setSecondAccount(BankAccount secondAccount) {
        this.secondAccount = secondAccount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean getTransactionType()
    {
        return transactionType;
    }

    public void setTransactionType(boolean transactionType) {
        this.transactionType = transactionType;
    }


    @Override
    public String toString() {
        String s;
        s = "Transaction ID: " + transactionId + ", Account: " + firstAccount.serialNumber;
        if(transactionType==false)
        {
            s += " sends " + amount + " to ";
        }
        else
        {
            s += " receives " + amount + " from ";
        }
        s +="account " + secondAccount.serialNumber;
        return s;
    }

    public void makeTransaction()
    {
        if(!transactionType)
        {
            firstAccount.currentAccountBalance -= amount;
            secondAccount.currentAccountBalance += amount;
        }
        else
        {
            firstAccount.currentAccountBalance += amount;
            secondAccount.currentAccountBalance -= amount;
        }
        firstAccount.addLogTransaction(this);
        secondAccount.addLogTransaction(new Transactions(transactionId, secondAccount, firstAccount, amount, !transactionType));
    }
}
