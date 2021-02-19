package com.company;


import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        /*
           (0)--(1)--(2)
            |   / \   |
            |  /   \  |
            | /     \ |
           (3)-------(4)    */
        ArrayList<ArrayList<Integer>> graph1 = new ArrayList(List.of(
                new ArrayList(List.of(0, 1, 0, 1, 0)),
                new ArrayList(List.of(1, 0, 1, 1, 1)),
                new ArrayList(List.of(0, 1, 0, 0, 1)),
                new ArrayList(List.of(1, 1, 0, 0, 1)),
                new ArrayList(List.of(0, 1, 1, 1, 0))
        ));

        graph1.forEach(System.out::println);

         /*
           (0)--(1)--(2)
            |   / \   |
            |  /   \  |
            | /     \ |
           (3)       (4)    */
        ArrayList<ArrayList<Integer>> graph2 = new ArrayList(List.of(
                new ArrayList(List.of(0, 1, 0, 1, 0)),
                new ArrayList(List.of(1, 0, 1, 1, 1)),
                new ArrayList(List.of(0, 1, 0, 0, 1)),
                new ArrayList(List.of(1, 1, 0, 0, 0)),
                new ArrayList(List.of(0, 1, 1, 0, 0))
        ));

        var startTime = System.nanoTime();

        try{
            DirectedGraph directedGraph1 = new DirectedGraph(graph1);
            directedGraph1.start();

            //DirectedGraph directedGraph2 = new DirectedGraph(graph2);
            //directedGraph2.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        var endTime = System.nanoTime();

        System.out.println(endTime-startTime);
    }
}
