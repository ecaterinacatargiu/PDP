package com.company;

import mpi.MPI;

import java.text.ParsePosition;


public class PollyOperations {

    //Master for simple multiplication
    public static void startSimpleMultiplication(Polly p1, Polly p2){

        int n = MPI.COMM_WORLD.Size();
        int[] coeffP1 = p1.getCoefficients();
        int[] coeffP2 = p2.getCoefficients();

        var startPos = 0;
        var stopPos = 0;
        var coeffPerNode = coeffP1.length/(n-1);

        for(int i = 1 ;i<n;i++)
        {
            startPos = stopPos;
            stopPos = startPos + coeffPerNode;

            if (i == n-1) {
                stopPos = coeffP1.length;
            }

            int[] bufferStart = new int[1];
            int[] bufferStop = new int[1];
            bufferStart[0] = startPos;
            bufferStop[0] = stopPos;

            MPI.COMM_WORLD.Send(coeffP1, 0, coeffP1.length,MPI.INT,i,0);
            MPI.COMM_WORLD.Send(coeffP2, 0,coeffP2.length,MPI.INT,i,0);
            MPI.COMM_WORLD.Send(bufferStart, 0,1,MPI.INT,i,0);
            MPI.COMM_WORLD.Send(bufferStop, 0,1,MPI.INT,i,0);
        }

        int[] result = new int[2*coeffP1.length-1];
        for(int i = 1 ;i<n;i++) {
            int[] currentResult = new int[2*coeffP1.length-1];
            MPI.COMM_WORLD.Recv(currentResult, 0, 2*coeffP1.length-1, MPI.INT, i, 0);

            for(int j = 0; j<2*coeffP1.length-1;j++)
            {
                result[j] +=currentResult[j];
            }
        }
        var r = new Polly(result);
        System.out.println("Final result of rank " + MPI.COMM_WORLD.Rank() + " is: ");
        System.out.println(r.toString());
    }

    //Worker for simple multiplication
    public static void simpleMultiplicationPartial(int pollySize) {

        int[] first = new int[pollySize];
        int[] second = new int[pollySize];
        int[] start = new int[1];
        int[] stop = new int[1];
        int[] re = new int[2*pollySize-1];

        MPI.COMM_WORLD.Recv(first, 0, pollySize, MPI.INT, 0, 0);
        MPI.COMM_WORLD.Recv(second, 0, pollySize, MPI.INT, 0, 0);
        MPI.COMM_WORLD.Recv(start, 0, 1, MPI.INT, 0, 0);
        MPI.COMM_WORLD.Recv(stop, 0, 1, MPI.INT, 0, 0);

        for (int i = start[0]; i < stop[0]; i++) {

            for (int j = 0; j < pollySize; j++) {
                re[i + j] += first[i] * second[j];
            }
        }

        var a = new Polly(re);
        System.out.println("Partial result of rank " + MPI.COMM_WORLD.Rank() + " is: ");
        System.out.println(a.toString());
        MPI.COMM_WORLD.Send(re, 0, re.length, MPI.INT, 0, 0);
    }

    public static void startKaratsuba(Polly p1, Polly p2,int nrProcesses){

        var result = new Polly(karatsubaRecursive(p1.getCoefficients(),p2.getCoefficients(),nrProcesses,0));
        System.out.println("Final result: ");
        System.out.println(result);
    }

    //Master for karatsuba multiplication
    public static int[] karatsubaRecursive(int[] p1, int[] p2,int nrProcesses,int currentId)
    {
        //base case
        int[] product = new int[2*p1.length];
        if(p1.length==1){
            product[2*p1.length-1] = p1[0]*p2[0];
            System.out.println("Partial result in master: ");
            var temp = new Polly(product);
            System.out.println(temp.toString());
            return product;
        }

        int halfSize = p1.length/ 2;

        //half arrays
        var aLow = new int[halfSize];
        var aHigh = new int[halfSize];
        var bLow = new int[halfSize];
        var bHigh = new int[halfSize];
        var aLowHigh = new int[halfSize];
        var bLowHigh = new int[halfSize];
        //split data
        for (int i = 0; i < halfSize; i++) {
            aLow[i] = p1[i];
            aHigh[i] = p1[halfSize+i];
            aLowHigh[i] = aHigh[i] + aLow[i];

            bLow[i] = p2[i];
            bHigh[i] = p2[halfSize+i];
            bLowHigh[i] = bHigh[i] + bLow[i];
        }

        int[] resultLow, resultLowHigh;
        int[] resultHigh;

        //Since I am running on a quadcore processor I split the work between 1 main thread and 3 worker nodes
        if(nrProcesses>=3)
        {
            //make sure we have at least 3 processes to use
            int[] lengths = new int[4];
            lengths[0] = nrProcesses/3;
            lengths[1] = aLow.length;
            lengths[2] = bLow.length;
            lengths[3] = currentId;

            MPI.COMM_WORLD.Send(lengths,0,lengths.length,MPI.INT,currentId+lengths[0],1);
            MPI.COMM_WORLD.Send(aLow,0,aLow.length,MPI.INT,currentId+lengths[0],2);
            MPI.COMM_WORLD.Send(bLow,0,bLow.length,MPI.INT,currentId+lengths[0],3);

            lengths[1] = aLowHigh.length;
            lengths[2] = bLowHigh.length;

            MPI.COMM_WORLD.Send(lengths,0,lengths.length,MPI.INT,currentId+2*lengths[0],1);
            MPI.COMM_WORLD.Send(aLowHigh,0,aLowHigh.length,MPI.INT,currentId+2*lengths[0],2);
            MPI.COMM_WORLD.Send(bLowHigh,0,bLowHigh.length,MPI.INT,currentId+2*lengths[0],3);

            resultHigh = karatsubaRecursive(aHigh,bHigh,lengths[0],currentId);

            //get the results
            int[] lowSize = new int[1];
            int[] lowHighSize = new int[1];
            MPI.COMM_WORLD.Recv(lowSize,0,1,MPI.INT,currentId+lengths[0],4);
            MPI.COMM_WORLD.Recv(lowHighSize,0,1,MPI.INT,currentId+2*lengths[0],4);
            resultLow = new int[lowSize[0]];
            resultLowHigh = new int[lowHighSize[0]];

            MPI.COMM_WORLD.Recv(resultLow,0,resultLow.length,MPI.INT,currentId+lengths[0],5);
            MPI.COMM_WORLD.Recv(resultLowHigh,0,resultLowHigh.length,MPI.INT,currentId+2*lengths[0],5);

        }
        else
        {
            resultLow = karatsubaRecursive(aLow,bLow,1,currentId);
            resultHigh = karatsubaRecursive(aHigh,bHigh,1,currentId);
            resultLowHigh = karatsubaRecursive(aLowHigh,bLowHigh,1,currentId);
        }

        int[] resultMiddle = new int[p1.length];

        for (int i = 0; i < p1.length; ++i) {
            resultMiddle[i] = resultLowHigh[i] - resultLow[i] - resultHigh[i];
        }

        for (int i = 0; i < p1.length; i++) {
            product[i] += resultLow[i];
            product[i + p1.length] += resultHigh[i];
            product[i + halfSize] += resultMiddle[i];
        }

        var temp = new Polly(product);
        System.out.println("Partial result in master: ");
        System.out.println(temp.toString());

        return product;
    }

    //Worker for karatsuba multiplication
    static void partialKaratsuba(int currentId) {

        int[] lenghts = new int[4];

        MPI.COMM_WORLD.Recv(lenghts, 0, 4, MPI.INT, MPI.ANY_SOURCE, 1);
        int[] a = new int[lenghts[1]];
        int[] b = new int[lenghts[2]];

        int source = lenghts[3];
        MPI.COMM_WORLD.Recv(a, 0, lenghts[1], MPI.INT, source, 2);
        MPI.COMM_WORLD.Recv(b, 0, lenghts[2], MPI.INT, source, 3);

        int[] result = karatsubaRecursive(a, b, lenghts[3], currentId);

        System.out.println(source);

        var poli = new Polly(result);
        System.out.println("Partial result in worker: ");
        System.out.println(poli.toString());

        int[] resultSize = new int[1];
        resultSize[0] = result.length;

        MPI.COMM_WORLD.Send(resultSize, 0, 1, MPI.INT, source, 4);
        MPI.COMM_WORLD.Send(result, 0, result.length, MPI.INT, source, 5);
    }
}
