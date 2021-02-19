package com.company;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.company.Matrix.*;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        var start = System.currentTimeMillis()/1000.0;

        var a = Matrix.generateRandomMatrix(9);
        var b = Matrix.generateRandomMatrix(9);

        System.out.println("Matrix a: ");
        a.getMatrix().forEach(System.out::println);
        System.out.println("\nMatrix b: ");
        b.getMatrix().forEach(System.out::println);

        MatrixOperations ops = new MatrixOperations(2,a,b);

        ops.threadsOnRows();
        //ops.threadsOnCols();
        //ops.threadsOnK();

        //ops.threadPoolOnRows();
        //ops.threadPoolOnCols();
        //ops.threadPoolOnK();

        var end = System.currentTimeMillis()/1000.0;
        System.out.println(end - start);
    }
}
