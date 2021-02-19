package com.company;
import static com.company.MatrixOperations.*;


public class ThreadOnColumnsTask implements Runnable{

    public int firstIndex;
    public int lastIndex;
    private String threadName;

    public ThreadOnColumnsTask(int firstIdx, int lastIdx, String name)
    {
        this.firstIndex = firstIdx;
        this.lastIndex = lastIdx;
        this.threadName = name;
    }

    @Override
    public void run() {
        Matrix a = MatrixOperations.a;
        Matrix b = MatrixOperations.b;
        int size = MatrixOperations.resultThreadsCol.getSize();
        synchronized (resultThreadsCol)
        {
            for(int i=firstIndex;i<lastIndex;i++)
            {
                var resultElement = Matrix.computeElement(a.getRow((size*(i%size)+i/size) / size),
                        b.getCol((size*(i%size)+i/size) % size));
                resultThreadsCol.setElement((size*(i%size)+i/size) / size, ((size*(i%size)+i/size) % size), resultElement);
            }
            System.out.println("\nPartial result of " + threadName);
            resultThreadsCol.getMatrix().forEach(System.out::println);
        }
    }
}
