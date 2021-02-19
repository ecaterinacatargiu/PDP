package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        var polly1 = new Polly(new ArrayList<Integer>(List.of(1, 2, 1, 1)));//,1,1,1,1,1,1,1,1,1,1,1,1)));
        var polly2 = new Polly(new ArrayList<Integer>(List.of(1, 2, 1, 1)));//,1,1,1,1,1,1,1,1,1,1,1,1)));

       //PollyOperations.startMultiplicationSequential(polly1, polly2);
        //PollyOperations.startMultiplicationParallelized(polly1, polly2, 3);
        PollyOperations.startkaratsubaSequential(polly1, polly2);
        PollyOperations.startKaratusbaParallel(polly1, polly2);
    }
}
