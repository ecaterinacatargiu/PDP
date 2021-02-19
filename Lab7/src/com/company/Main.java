package com.company;

import mpi.MPI;


public class Main {

    public static void main(String[] args) {

        MPI.Init(args);
        var startTime = System.currentTimeMillis()/1000.0;
        int rank = MPI.COMM_WORLD.Rank();
        int polySize = 4;

        //Master
        if(rank == 0)
        {
            var p1 = new Polly(new int[]{1, 2, 1, 1});//,1,1,1,1,1,1,1,1,1,1,1,1)));
            var p2 = new Polly(new int[]{1, 2, 1, 1});//,1,1,1,1,1,1,1,1,1,1,1,1)));
            System.out.println(p1);
            System.out.println(p2);

            PollyOperations.startSimpleMultiplication(p1, p2);
            //PollyOperations.startKaratsuba(p1, p2,MPI.COMM_WORLD.Size());

        }
        //Worker
        else
        {
            //PollyOperations.partialKaratsuba(polySize);
            PollyOperations.simpleMultiplicationPartial(polySize);

            //PollyOperations.partialKaratsuba(rank);
        }

        var endTime = System.currentTimeMillis()/1000.0;

        System.out.println(endTime - startTime);

        MPI.Finalize();
    }
}

