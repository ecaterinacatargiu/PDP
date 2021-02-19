package com.company;
import static com.company.MatrixOperations.*;


public class ThreadOnRowsTask implements Runnable{

    private int firstIndex;
    private int lastIndex;
    private String threadName;

    public ThreadOnRowsTask(int firstIfx, int lastIdx, String name)
    {
        this.firstIndex = firstIfx;
        this.lastIndex = lastIdx;
        this.threadName = name;
    }

    @Override
    public void run() {
        Matrix a = MatrixOperations.a;
        Matrix b = MatrixOperations.b;
        int size = resultThreadsRow.getSize();
        //synchronized (resultThreadsRow) {
            for (int i = firstIndex; i <= lastIndex; i++) {

                var resultElement = Matrix.computeElement(a.getRow(i / size), b.getCol(i % size));
                resultThreadsRow.setElement(i / size, i % size, resultElement);
            }
            synchronized (resultThreadsRow) {
                System.out.println("\nPartial result of " + threadName);


                resultThreadsRow.getMatrix().forEach(System.out::println);
            }
        //}
    }
}
