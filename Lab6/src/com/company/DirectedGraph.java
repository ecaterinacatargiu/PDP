package com.company;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class DirectedGraph {

    private ArrayList<ArrayList<Integer>> graph;


    DirectedGraph(ArrayList<ArrayList<Integer>> initialGraph)
    {
        graph = initialGraph;
    }

    public void start()
    {
        ArrayList<Integer> path = new ArrayList<Integer>();

        IntStream.range(0,graph.size()).forEach(e -> path.add(-1));

        try {
            path.set(0,0);
            searchHamilltonian(0, path, 1);
            throw new Exception("No solution found");
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void searchHamilltonian(int currentVertex, ArrayList<Integer> path, int pathSize) throws Exception {
        System.out.println(path);

        //A solution was found so we only print the path
        if(graph.get(currentVertex).get(0) == 1 && pathSize == graph.size()){
            System.out.println(path);

            throw new Exception("Solution found");
        }
        //All the vertices are visited, then return
        if(pathSize == graph.size()){
            return;
        }

        //Start checking all vertices
        for(int i=0;i<graph.size();i++){

            //If there is an edge we add it to the path and temporarily remove it - this is how I mark an edge as visited
            if(graph.get(currentVertex).get(i)==1){

                path.set(pathSize++,i);
                graph.get(currentVertex).set(i,0);
                graph.get(i).set(currentVertex,0);

                //If we didn't check the path of this vertex I call this function recursively on a new thread
                if(!isVisited(i,path,pathSize)){

                    ExecutorService ex = Executors.newSingleThreadExecutor();
                    final int vertex = i;
                    final int count = pathSize;

                    final Runnable task = () -> {
                        try{
                            searchHamilltonian(vertex,path,count);

                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    };
                    ex.submit(task).get();
                }

                //Replace the removed edge so the paths can be correctly returned
                graph.get(currentVertex).set(i, 1);
                graph.get(i).set(currentVertex, 1);

                //Delete the path after it was checked
                path.set(--pathSize,-1);
            }
        }
    }

    //Checks if a edge is visited or not
    public boolean isVisited(int vertex, ArrayList<Integer> path, int pathSize)
    {
        for(int i=0; i < pathSize-1;i++)
        {
            if(path.get(i) == vertex)
                return true;
        }
        return false;
    }






}
