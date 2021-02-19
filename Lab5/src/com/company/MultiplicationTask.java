package com.company;

import java.util.ArrayList;

public class MultiplicationTask implements Runnable{

    private Polly a, b;
    private ArrayList<Integer> result;
    private int start, stop;

    public MultiplicationTask(Polly a, Polly b, ArrayList<Integer> result, int startt, int stopp) {
        this.a = a;
        this.b = b;
        this.result = result;
        this.start = startt;
        this.stop = stopp;
    }

    @Override
    public void run() {

        synchronized (result) {

            for (int i = start; i < stop; i++) {
                for (int j = 0; j < b.getDegree(); j++) {
                    result.set(i + j, result.get(i +j) + a.getCoefficients().get(i) * b.getCoefficients().get(j));
                }
            }
        }
    }
}
