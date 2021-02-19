import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Controller {

        private List<Thread> threads;
        private Repository repository;
        private static List<Transactions> transactions = new ArrayList<>();

    public Controller(Repository repo)
        {
            this.threads = new ArrayList<Thread>();
            this.repository = repo;
        }

        public void startThreads(int nrThreads) throws InterruptedException {
            for(int i=0;i<nrThreads;i++)
            {
                threads.add(new Thread(repository));
            }

            transactions.stream().forEach(t -> threads.add(new Thread((Runnable) t)));

            for (Thread thread : threads){
                thread.start();
            }

            for (Thread thread : threads){
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

}
