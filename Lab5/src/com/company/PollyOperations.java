package com.company;

import org.w3c.dom.html.HTMLImageElement;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PollyOperations {

    public static void startMultiplicationSequential(Polly p1, Polly p2)
    {
        var start = System.currentTimeMillis()/1000.0;

        ArrayList<Integer> resultCoefficients = new ArrayList<>();

        Polly result;

        for (int i = 0; i < p1.getDegree()*2-1 ; i++){
            resultCoefficients.add(i,0);
        }

        for (int i = 0; i < p1.getCoefficients().size(); i++) {
            // Multiply the current term of first polynomial with every term of second polynomial.
            for (int j = 0; j < p2.getCoefficients().size(); j++) {
                resultCoefficients.set(i + j,  resultCoefficients.get(i+j) + p1.getCoefficients().get(i) * p2.getCoefficients().get(j));
            }
        }
        result = new Polly(resultCoefficients);
        System.out.println("The result of polinomial simple " + p1.toString() + " multiplied with " + p2.toString() + "is: ");
        System.out.println(result.toString());

        var end = System.currentTimeMillis()/1000.0;

        System.out.println(end - start);
    }

    public static void startMultiplicationParallelized(Polly p1, Polly p2, int threads) throws InterruptedException {

        var start = System.currentTimeMillis()/1000.0;

        ArrayList<Integer> resultCoefficients = startThreads(p1, p2, threads);

        var result = new Polly(resultCoefficients);
        System.out.println("The result of polinomial parallel " + p1.toString() + " multiplied with " + p2.toString() + "is: ");
        System.out.println(result.toString());

        var end = System.currentTimeMillis()/1000.0;

        System.out.println(end - start);
    }

    private static ArrayList<Integer> startThreads(Polly p1, Polly p2, int threads) throws InterruptedException {

        var coeffPerThread = p2.getDegree()   / threads;
        var rest = p2.getDegree() % threads;

        var start = 0;
        var stop = 0;

        ArrayList<Integer> result = new ArrayList<>();
        Thread multiplicationThread[] = new Thread[threads];

        for (int i = 0; i < p1.getDegree()*2-1 ; i++){
            result.add(i,0);
        }

        for (int i = 0; i < threads; i++) {

            stop = start + coeffPerThread;
            if (rest != 0) {
                stop++;
                rest--;
            }

            multiplicationThread[i] = new Thread(new MultiplicationTask(p1, p2, result, start, stop));
            multiplicationThread[i].start();
            start = stop;
        }

        for (int i = 0; i < threads; i++) {
            multiplicationThread[i].join();
        }

        return result;

    }

    public static void startkaratsubaSequential(Polly p1, Polly p2)
    {
        var startTime = System.currentTimeMillis()/1000.0;

        System.out.println(p1.getCoefficients());
        System.out.println(p2.getCoefficients());

        var result = karatsubaSequential(p1.getCoefficients(),p2.getCoefficients());

        var polyResult = new Polly(result);
        System.out.println("The result of sequential karatusba polinomial " + p1.toString() + " multiplied with " + p2.toString() + "is: ");
        System.out.println(polyResult.toString());

        var endTime = System.currentTimeMillis()/1000.0;

        System.out.println(endTime - startTime);
    }

    public static ArrayList<Integer> karatsubaSequential(ArrayList<Integer> p1, ArrayList<Integer> p2) {
        ArrayList<Integer> product = new ArrayList<Integer>();

        IntStream.range(0,2*p2.size()).forEach(e -> {
            product.add(e,0);
        });

        if (p2.size() == 1) {
            product.set(product.size()-1, p1.get(0) * p2.get(0));
            return product;
        }

        int halfSize = p1.size() / 2;

        //Half arrays
        var aLow = new ArrayList<Integer>();
        var aHigh = new ArrayList<Integer>();
        var bLow = new ArrayList<Integer>();
        var bHigh = new ArrayList<Integer>();
        var aLowHigh = new ArrayList<Integer>();
        var bLowHigh = new ArrayList<Integer>();

        IntStream.range(0,halfSize).forEach(e -> {
            aLow.add(e,0);
            aHigh.add(e,0);
            bLow.add(e,0);
            bHigh.add(e,0);
            aLowHigh.add(e,0);
            bLowHigh.add(e,0);

        });

        //Fill low and high arrays
        for (int i = 0; i < halfSize; i++) {
            aLow.set(i, p1.get(i));
            aHigh.set(i, p1.get(halfSize + i));
            aLowHigh.set(i, aHigh.get(i) + aLow.get(i));

            bLow.set(i, p2.get(i));
            bHigh.set(i, p2.get(halfSize + i));
            bLowHigh.set(i, bHigh.get(i) + bLow.get(i));
        }

        var productLow = karatsubaSequential(aLow, bLow);
        var productHigh = karatsubaSequential(aHigh, bHigh);
        var productLowHigh = karatsubaSequential(aLowHigh, bLowHigh);

        //Construct Temp portion of the product
        var productTemp = new ArrayList<Integer>();

        IntStream.range(0,p1.size()).forEach(e -> {
            productTemp.add(e,0);
        });

        for (int i = 0; i < p1.size(); ++i) {
            productTemp.set(i, productLowHigh.get(i) - productLow.get(i) - productHigh.get(i));
        }

        //Assemble the product from the low, Temp and high parts
        System.out.println("-----");
        System.out.println("Low" + productLow);
        System.out.println("Temp" + productTemp);
        System.out.println("High" + productHigh);
        int midOffset = p1.size() / 2 ;

        for (int i = 0; i < p1.size(); i++) {
            product.set(i, product.get(i) + productLow.get(i));
            product.set(i + p1.size(), product.get(i + p1.size()) + productHigh.get(i));
            product.set(i + midOffset , product.get(i + midOffset) + productTemp.get(i));
        }
        return product;
    }


    public static void startKaratusbaParallel(Polly p1, Polly p2) throws ExecutionException, InterruptedException {

        var start = System.currentTimeMillis()/1000.0;

        System.out.println(p1.getCoefficients());
        System.out.println(p2.getCoefficients());

        var result = karatsubaParallelized(p1.getCoefficients(),p2.getCoefficients());

        var polyResult = new Polly(result);

        System.out.println("The result of parallel karatusba polinomial " + p1.toString() + " multiplied with " + p2.toString() + "is: ");
        System.out.println(polyResult.toString());

        var end = System.currentTimeMillis()/1000.0;

        System.out.println(end - start);

    }

    public static ArrayList karatsubaParallelized(ArrayList<Integer> p1, ArrayList<Integer> p2) throws ExecutionException, InterruptedException {
        ArrayList<Integer> product = new ArrayList<Integer>();

        IntStream.range(0,2*p2.size()).forEach(e -> {
            product.add(e,0);
        });

        if (p2.size() == 1) {
            product.set(product.size()-1, p1.get(0) * p2.get(0));

            return product;
        }

        int halfSize = p1.size() / 2;

        //Half arrays
        var aLow = new ArrayList<Integer>();
        var aHigh = new ArrayList<Integer>();
        var bLow = new ArrayList<Integer>();
        var bHigh = new ArrayList<Integer>();
        var aLowHigh = new ArrayList<Integer>();
        var bLowHigh = new ArrayList<Integer>();

        IntStream.range(0,halfSize).forEach(e -> {
            aLow.add(e,0);
            aHigh.add(e,0);
            bLow.add(e,0);
            bHigh.add(e,0);
            aLowHigh.add(e,0);
            bLowHigh.add(e,0);

        });

        //Fill low and high arrays
        for (int i = 0; i < halfSize; i++) {
            aLow.set(i, p1.get(i));
            aHigh.set(i, p1.get(halfSize + i));
            aLowHigh.set(i, aHigh.get(i) + aLow.get(i));

            bLow.set(i, p2.get(i));
            bHigh.set(i, p2.get(halfSize + i));
            bLowHigh.set(i, bHigh.get(i) + bLow.get(i));
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        Callable<ArrayList<Integer>> task1 = () -> karatsubaSequential(aLow, bLow);
        Callable<ArrayList<Integer>> task2 = () -> karatsubaSequential(aHigh, bHigh);
        Callable<ArrayList<Integer>> task3 = () -> karatsubaSequential(aLowHigh, bLowHigh);

        Future<ArrayList<Integer>> futureProductLow = executor.submit(task1);
        Future<ArrayList<Integer>> futureProductHigh = executor.submit(task2);
        Future<ArrayList<Integer>> futureProductLowHigh = executor.submit(task3);

        var productLow = futureProductLow.get();
        var productHigh = futureProductHigh.get();
        var productLowHigh = futureProductLowHigh.get();

        executor.shutdown();

        //Construct Temp portion of the product
        var productTemp = new ArrayList<Integer>();

        IntStream.range(0,p1.size()).forEach(e -> {
            productTemp.add(e,0);
        });

        for (int i = 0; i < p1.size(); ++i) {
            productTemp.set(i, productLowHigh.get(i) - productLow.get(i) - productHigh.get(i));
        }

        //Assemble the product from the low, Temp and high parts
        System.out.println("-----");
        System.out.println("Low: " + productLow);
        System.out.println("Temp: " + productTemp);
        System.out.println("High: " + productHigh);

        int midOffset = p1.size() / 2 ;

        for (int i = 0; i < p1.size(); i++) {
            product.set(i, product.get(i) + productLow.get(i));
            product.set(i + p1.size(), product.get(i + p1.size()) + productHigh.get(i));
            product.set(i + midOffset , product.get(i + midOffset) + productTemp.get(i));

        }
        return product;
    }
}
