import java.time.ZonedDateTime;

public class Main
{

    public static void main(String[] args) throws InterruptedException {

        Repository repository = new Repository();
        repository.initBankAccount(1000);
        Controller ctrl = new Controller(repository);
        ctrl.startThreads(10);
        repository.checkAccounts();

    }
}
