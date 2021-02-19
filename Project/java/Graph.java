package com.company;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Graph {

    private int nrEdges;
    private int nrVertices;

    //An independent set is a set of vertices in a graph where no two of which are adjacent
    private final Set<Integer> independentSet;

    private final Map<Integer, Node> vertexInfo;

    private Set<Integer> colors;

    private Map<Integer, Set<Integer>> graph;

    private ExecutorService executorService;

    public Graph(int nbOfVertices)
    {
        this.nrVertices = nbOfVertices;
        graph = new HashMap<>();
        vertexInfo = new HashMap<>();
        for (int i = 0; i < nbOfVertices; i++)
        {
            graph.put(i, new HashSet<>());
            vertexInfo.put(i, new Node(-1, getRandomNumberInRange(0, 100)));
        }
        colors = new TreeSet<>();
        independentSet = new HashSet<>(graph.keySet());

        //Choose how many colors you want
        for (int i = 0; i < 100; i++) {
            colors.add(i);
        }
        executorService = Executors.newFixedThreadPool(8);
    }

    /*Description: A method used to get a random number from a given range
    *Input: min - the lower bound
    *       max -  upper bound
    * Output: an integer between the given bounds
    * */
    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("Max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    /*Description: A method used to add a new edge between 2 vertices
    * Input: source - the vertex we want to start the edge from
    *       desctination - the vertex we want to finish the edge in
    * Output: -
    * */
    public void addEdge(int source, int destination) {
        nrEdges++;
        graph.get(source).add(destination);
        graph.get(destination).add(source);
    }

    /*Description: A method used to get the neighbours of a given vertex
    *Input: vertex - the vertex for which we search the neighbours
    * Output: the list of neighbours of that vertex
    * */
    public List<Integer> getNeighbours(int vertex) {
        return graph.get(vertex).stream().filter((node) -> {
            return getColor(node) == -1;
        }).collect(Collectors.toList());
    }

    /*Description: A method used to color the graph
    *Input: -
    *Ouput: the colored graph
    * */
    public void colorGraph() {
        while (!independentSet.isEmpty()) {
            Set<Integer> set = getIndependentSet();
            for (Integer v : set) {
                executorService.submit(() -> {
                    setColor(v);
                });
            }
            independentSet.removeAll(set);
        }
        executorService.shutdown();
    }

    /*Description: A method used to pretty print the colors used to color a graph, for each vertex individually*/
    public void printColors() {
        for (Integer v : vertexInfo.keySet())
            System.out.println("V:" + v + " -> color:" + getColor(v));
    }

    /*Description: A method used to get the value from a vertex
    *Input: vertex - the vertex we want the value from
    * Output: the value of the vertex
    * */
    public Integer getValue(int vertex) {
        return vertexInfo.get(vertex).random;
    }


    /*Description: A method that returns the color of a given vertex
    * Input: the vertex we want to get the color from
    * Output: the color on that vertex*/
    public Integer getColor(int vertex) {
        return vertexInfo.get(vertex).color;
    }

    private boolean checkVertex(int vertex) {
        for (Integer neighbour : getNeighbours(vertex)) {
            if (getValue(neighbour) > getValue(vertex))
                return false;
            else if (getValue(neighbour) == getValue(vertex) && vertex > neighbour)
                return false;
        }
        return true;
    }

    /*Description: A method used to get the neighbours' colors of a vertex
    *Input: the vertex we want to check
    * Output: the set of that vertex neighbours' colors
    * */
    public Set<Integer> getNeighboursColors(int vertex) {
        return getAllNeighbours(vertex).stream().filter((node) -> {
            return getColor(node) != -1;
        }).map(node -> getColor(node)).distinct().collect(Collectors.toSet());
    }

    /*Description: A method used to get all the neighbours of a given vertex
    *Input: vertex - the vertex we want to get the neighbours from
    * Output: the neighbours of that vertex
    * */
    public Set<Integer> getAllNeighbours(int vertex) {
        return graph.get(vertex);
    }

    /*Description: A method that gets the smallest possible color for a vertex
    *Input: vertex - the vertex we want to get the smallest color for
    * Output: the desired color
    * */
    public Integer getSmallestColor(int vertex) {
        Set<Integer> neighColors = getNeighboursColors(vertex);
        for (Integer c : colors) {
            if (!neighColors.contains(c))
                return c;
        }
        return 0;
    }

    /*Desctiption: A method that puts a color on a given vertex
    *Input: vertex - the vertex we want to color
    * Output: -
    * */
    public void setColor(int vertex) {
        vertexInfo.get(vertex).color = getSmallestColor(vertex);
    }

    /*Description: A method that returns the independent set of a graph
    *Input: -
    * Output: the independent set of the graph(an independent set is a set of vertices in a graph where no two of which are adjacent)
    * */
    public Set<Integer> getIndependentSet() {
        ArrayList<Future<Boolean>> list = new ArrayList<>();
        Set<Integer> res = new HashSet<>();
        List<Integer> l = new ArrayList<>(independentSet);
        for (Integer v : l) {
            Future<Boolean> f = executorService.submit(() -> checkVertex(v));
            list.add(f);
        }

        for (int i = 0; i < independentSet.size(); i++) {
            try {
                if (list.get(i).get())
                    res.add(l.get(i));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
