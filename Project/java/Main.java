package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        FileReader file = new FileReader(args[0]);
        BufferedReader reader = new BufferedReader(file);

        int nrVertices;
        int nrEdges;

        String lineSplit[] = new String[2];

        String line = reader.readLine();

        lineSplit = line.split("\\s+");

        nrVertices = Integer.parseInt(lineSplit[2]);
        nrEdges = Integer.parseInt(lineSplit[3]);

        Graph g = new Graph(nrVertices+1);

        String line1 = null;

        while((line1 = reader.readLine()) !=null)
        {
            lineSplit = line1.split("\\s+");
            g.addEdge(Integer.parseInt(lineSplit[1]), Integer.parseInt(lineSplit[2]));
        }
        reader.close();

        long startTime = System.nanoTime();
        g.colorGraph();
        long endTime = System.nanoTime();

        long elapsedTime = endTime - startTime;
        double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
        //g.printColors();

        System.out.println(elapsedTimeInSecond);
    }
}

