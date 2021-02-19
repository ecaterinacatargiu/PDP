package com.company;

public class Polly {

    private int[] coefficients ;

    public Polly(int[] coeffs) {
        this.coefficients = coeffs;
    }

    public int[] getCoefficients(){
        return this.coefficients;
    }

    public int getDegree() {
        return coefficients.length;
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();
        for (int index = 0; index<coefficients.length;index++) {
            if(coefficients[index] !=0) {
                String sign = (coefficients[index] > 0) ? " + " : " - ";
                result.append(sign + coefficients[index] + "*x^" + (coefficients.length - index - 1) + " ");
            }
        }
        return result.toString();
    }
}
