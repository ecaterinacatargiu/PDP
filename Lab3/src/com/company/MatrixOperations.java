package com.company;

import com.sun.source.tree.MemberReferenceTree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import static com.company.MatrixOperations.*;

public class MatrixOperations {

    private int threads;
    public static Matrix a,b,resultThreadsRow,resultThreadsCol,resultThreadsK;

    public MatrixOperations(int nrThreads, Matrix aa, Matrix bb){
        threads=nrThreads;
        a=aa;
        b=bb;
        resultThreadsRow = new Matrix(9);
        resultThreadsCol = new Matrix(9);
        resultThreadsK = new Matrix(9);
    }

    public void threadPoolOnRows() throws InterruptedException {
        Runnable t1 = new ThreadOnRowsTask(0,19,"1");
        Runnable t2 = new ThreadOnRowsTask(20,39,"2");
        Runnable t3 = new ThreadOnRowsTask(40,59,"3");
        Runnable t4 = new ThreadOnRowsTask(60,80,"4");

        ExecutorService pool = Executors.newFixedThreadPool(2);

        pool.execute(t1);
        pool.execute(t2);
        pool.execute(t3);
        pool.execute(t4);

        if(!pool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
            pool.shutdownNow();

            System.out.println("\n Thread pool on rows result: ");
            resultThreadsRow.getMatrix().forEach(System.out::println);
        }
    }

    public void threadPoolOnK() throws InterruptedException {
        Runnable t1 = new ThreadKTask(0,4);
        Runnable t2 = new ThreadKTask(1,4);
        Runnable t3 = new ThreadKTask(2,4);
        Runnable t4 = new ThreadKTask(3,4);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        pool.execute(t1);
        pool.execute(t2);
        pool.execute(t3);
        pool.execute(t4);

        if(!pool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
            pool.shutdownNow();

            System.out.println("\n Thread pool on k result: ");
            resultThreadsK.getMatrix().forEach(System.out::println);
        }
    }

    public void threadPoolOnCols() throws InterruptedException {
        Runnable t1 = new ThreadOnColumnsTask(0,19,"1");
        Runnable t2 = new ThreadOnColumnsTask(20,39,"2");
        Runnable t3 = new ThreadOnColumnsTask(40,59,"3");
        Runnable t4 = new ThreadOnColumnsTask(60,80,"4");

        ExecutorService pool = Executors.newFixedThreadPool(2);

        pool.execute(t1);
        pool.execute(t2);
        pool.execute(t3);
        pool.execute(t4);

        if(!pool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
            pool.shutdownNow();

            System.out.println("\n Thread pool in col result: ");
            resultThreadsCol.getMatrix().forEach(System.out::println);
        }
    }

    public void threadsOnRows() throws InterruptedException {
        Thread t1 = new Thread(new ThreadOnRowsTask(0,19,"1"));
        Thread t2 = new Thread(new ThreadOnRowsTask(20,39,"2"));
        Thread t3 = new Thread(new ThreadOnRowsTask(40,59,"3"));
        Thread t4 = new Thread(new ThreadOnRowsTask(60,80,"4"));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        System.out.println("\n Simple thread on rows result: ");
        resultThreadsRow.getMatrix().forEach(System.out::println);
    }

    public void threadsOnCols() throws InterruptedException {
        Thread t1 = new Thread(new ThreadOnColumnsTask(0,20,"1"));
        Thread t2 = new Thread(new ThreadOnColumnsTask(20,40,"2"));
        Thread t3 = new Thread(new ThreadOnColumnsTask(40,60,"3"));
        Thread t4 = new Thread(new ThreadOnColumnsTask(60,80,"4"));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        System.out.println("\n Simple thread on cols result: ");
        resultThreadsCol.getMatrix().forEach(System.out::println);
    }

    public void threadsOnK() throws InterruptedException {
        Thread t1 = new Thread(new ThreadKTask(0,4));
        Thread t2 = new Thread(new ThreadKTask(1,4));
        Thread t3 = new Thread(new ThreadKTask(2,4));
        Thread t4 = new Thread(new ThreadKTask(3,4));

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();

        System.out.println("\n Simple thread on k result: ");
        resultThreadsK.getMatrix().forEach(System.out::println);
    }

}
