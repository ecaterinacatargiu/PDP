package com.company;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Polly {

    private ArrayList<Integer> coefficients;

    public Polly(ArrayList<Integer> coefficientss) {
        this.coefficients = coefficientss;
    }

    public ArrayList<Integer> getCoefficients(){
        return this.coefficients;
    }

    public int getDegree() {
        return coefficients.size();
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();
        for (int index = 0; index<coefficients.size();index++) {
            if(coefficients.get(index) !=0) {
                String sign = (coefficients.get(index) > 0) ? " + " : " - ";
                result.append(sign + coefficients.get(index) + "*x^" + (coefficients.size() - index - 1) + " ");
            }
        }
        return result.toString();
    }
}
