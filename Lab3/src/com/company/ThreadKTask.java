package com.company;
import static com.company.MatrixOperations.*;


public class ThreadKTask implements Runnable{

    private int taskNr;
    private int nrTasks;

    public ThreadKTask(int tasknr, int nrtasks)
    {
        this.taskNr = tasknr;
        this.nrTasks = nrtasks;
    }

    @Override
    public void run() {
        synchronized (resultThreadsK) {
            int size = resultThreadsK.getSize();
            for (int i = taskNr; i < size*size; i += nrTasks) {
                var resultElement = Matrix.computeElement(a.getRow(i / size), b.getCol(i % size));
                resultThreadsK.setElement(i / size, i % size, resultElement);
            }
            System.out.println("\nPartial result of task" + taskNr);
            resultThreadsK.getMatrix().forEach(System.out::println);
        }
    }

}
