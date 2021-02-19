import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Repository implements Runnable
{

    public List<BankAccount> accounts;
    public Lock allMutexes;

    public Repository()
    {
        accounts = new ArrayList<>();
        allMutexes = new ReentrantLock();
    }

    public List<BankAccount> getAccounts()
    {
        return accounts;
    }

    public void setAccounts(List<BankAccount> accounts)
    {
        this.accounts = accounts;
    }

    public void addAccount(BankAccount newAccount)
    {
        accounts.add(newAccount);
    }

    public void initBankAccount(int size)
    {
        for(int i=0;i<size;i++)
        {
            addAccount(new BankAccount());
        }
    }

    //here we smake a transaction
    @Override
    public void run(){
        Random rdm = new Random();
        int size = rdm.nextInt(250);
        int i;
        int first=0;
        int second=0;

        for(i=0;i<size;i++) {
            int firstBound = getAccounts().size();
            int secondBound = getAccounts().size();
            first = rdm.nextInt(firstBound);
            second = rdm.nextInt(secondBound);
            while (first == second)
            {
                second = rdm.nextInt(getAccounts().size());
            }
            BankAccount a1 = first < second ? accounts.get(first) : accounts.get(second);
            BankAccount a2 = first < second ? accounts.get(second) : accounts.get(first);

            System.out.println("Thread: " + Thread.currentThread().getId() + " Waiting for " + a1.serialNumber + " and " + a2.serialNumber);
            try
            {
                if (!a1.mutex.tryLock(30, TimeUnit.SECONDS) || !a2.mutex.tryLock(30, TimeUnit.SECONDS))
                    System.out.println("Deadlock. Abort transaction");
                else {
                    new Transactions(a1, a2, rdm.nextInt(50), true);
                    System.out.println("Thread: " + Thread.currentThread().getId() + " locked " + a1.serialNumber + " and " + a2.serialNumber);
                    a1.mutex.unlock();
                    a2.mutex.unlock();
                    System.out.println("Thread: " + Thread.currentThread().getId() + " released " + a1.serialNumber + " and " + a2.serialNumber);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try{
                if (rdm.nextInt() < 90)
                {
                    checkAccounts();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


    }

    public synchronized void checkAccounts() throws InterruptedException {
        if(allMutexes.tryLock(3, TimeUnit.SECONDS))
        {
            boolean ok = true;
            for(BankAccount account: accounts)
            {
                //System.out.println("Waiting for: " + account.serialNumber);
                account.mutex.tryLock();
            }
            for(BankAccount account: accounts)
            {
                if(!account.checkIfValid())
                {
                    ok = false;
                }
            }
            if(ok)
            {
                System.out.println("OK!!!");
            }
            else
            {
                System.out.println("FAIL!!!");
            }

            for(BankAccount account: accounts)
            {
                if(account.mutex.isHeldByCurrentThread())
                {
                    account.mutex.unlock();
                }
            }
            allMutexes.unlock();
        }
    }
}
