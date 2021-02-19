package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Matrix {

    private int size;
    private int currentRow;
    private int currentCol;
    private List<ArrayList<Integer>> matrix;

    public Matrix(int n)
    {
        this.size = n;

        matrix = new ArrayList<ArrayList<Integer>>(size);
        for(int i=0;i<size;i++) {
            matrix.add(new ArrayList<Integer>(Collections.nCopies(n, 0)));
        }
        currentRow=0;
        currentCol=0;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = (currentRow%size);
    }

    public int getCurrentCol() {
        return currentCol;
    }

    public void setCurrentCol(int currentCol) {
        this.currentRow = (currentRow%size);
    }

    public int getSize() {
        return size;
    }

    public List<ArrayList<Integer>> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<ArrayList<Integer>> matrix) {
        this.matrix = matrix;
    }

    public int getElement(int i, int j){
        return matrix.get(i).get(j);
    }

    public void setElement(int i, int j, int value){
        matrix.get(i).set(j,value);
    }

    public ArrayList<Integer> getRow(int index) {
        return matrix.get(index);
    }

    public ArrayList<Integer> getCol(int index) {
        var collector = new ArrayList<Integer>(size);
        IntStream.range(0,size).forEach(e ->collector.add(matrix.get(e).get(index)));
        return collector;
    }

    public static ArrayList<Integer> generateRow(int sizeOf) {
        ArrayList<Integer> result = new ArrayList<>();
        Random rand = new Random();
        IntStream.range(0,sizeOf).forEach(e -> result.add(rand.nextInt(10) + 1));
        return result;
    }

    public static Matrix generateRandomMatrix(int size) {
        var newMatrix = new Matrix(size);
        var els = new ArrayList<ArrayList<Integer>>(size);

        IntStream.range(0,size).forEach(e -> els.add(generateRow(size)));
        newMatrix.setMatrix(els);
        return newMatrix;
    }

    public static int computeElement(ArrayList<Integer> row, ArrayList<Integer> col){
        return IntStream.range(0, row.size()).map(e -> row.get(e) * col.get(e)).sum();
    }
}
