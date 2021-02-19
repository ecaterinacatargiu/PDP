package com.company;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumer {

    private LinkedList<Integer> consumer = new LinkedList<>();

    private LinkedList<Integer> vector1 = new LinkedList<>();
    private LinkedList<Integer> vector2 = new LinkedList<>();

    int size=100;
    int sum =0 ;

    public ProducerConsumer()
    {
    }

    public void makeRandomVectors()
    {
        Random random = new Random();
        for (int i=0;i<this.size;i++)
        {
            this.vector1.add(random.nextInt(10));
            this.vector2.add(random.nextInt(10));
        }
    }

    public void printVectors()
    {
        System.out.println(this.vector1);
        System.out.println(this.vector2);
        System.out.println();
    }

    public void produce() throws InterruptedException {
        {
            synchronized (this) {
                try {
                    for (int i = 0; i < size; i++) {
                        while (consumer.size() != 0)
                            wait();
                        int prod = vector1.get(i) * vector2.get(i);
                        consumer.add(prod);
                        System.out.println("Producer: " + vector1.get(i) + " x " + vector2.get(i) + " = " + prod);
                        notify();
                    }
                } finally {
                    //lock.unlock();
                }
            }
        }
    }

    public void consume() throws InterruptedException {
        synchronized (this)
        {
            try {
                for(int i=0;i<size;i++) {
                    while (consumer.size() == 0) {
                        wait();
                    }
                    sum += consumer.removeFirst();
                    System.out.println("Consumer: " + sum);
                    notify();
                }
            } finally {
                //lock.unlock();
            }
            System.out.println();
            System.out.println("Scalar product: " + sum);
        }
    }


}
