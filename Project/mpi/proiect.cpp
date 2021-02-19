#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <vector>
#include <iostream>
#include <algorithm>
#include <fstream>
#include <cstring>
#include <mpi.h>

#define CUSTOM_MPI_RANK_ROOT 0

int rank; 
int number_of_nodes;

int gVertices = 0;
int gEdges = 0;
int gUpperBoundChrom = 0;

std::vector<int> gGraph;
std::vector<int> gWeights;
std::vector<int> gColors;

bool compare(const int a, const int b)
{
    return (a > b) - (a < b);
}

int compare2(const void* a, const void* b)
{
    int la = *(const int*)a;
    int lb = *(const int*)b;

    return (la > lb) - (la < lb);
}

int getBiggestColor(std::vector<int> colors)
{
    return *std::max_element(std::begin(colors), std::end(colors));
}
void write_output(std::string filename)
{
    std::ofstream out_file(filename);

    for (int i = 0; i < gVertices; i++)
    {
        out_file << "Vertex= " << (i + 1) << " has color= " << gColors[i] << "\n";
    }
    out_file << "Largest color= " << getBiggestColor(gColors) << "\n";//gColors[gVertices - 1] << "\n";
    
    out_file.close();
}

void read_graph_from_file(std::string filename)
{
    std::ifstream in_file(filename);
    
    std::string vertex1 = std::string();
    std::string vertex2 = std::string();
    std::string token = std::string();
    std::string colp = std::string();
    std::string coledge = std::string();

    int edges_processed = 0;
    int max_degree = -1;
    int row_indx, col_indx;
    
    in_file >> colp >> coledge;
    in_file >> gVertices >> gEdges;

    gGraph = std::vector<int>(gVertices * gVertices);
    while (in_file >> coledge >> vertex1 >> vertex2)
    {
        row_indx = std::stoi(vertex1) - 1;
        col_indx = std::stoi(vertex2) - 1;

        gGraph[row_indx * gVertices + col_indx] = 1;
        gGraph[col_indx * gVertices + row_indx] = 1;

        edges_processed++;
    }

    if (gVertices & 2 == 0)
    {
        gUpperBoundChrom = gVertices - 1;
    }
    else
    {
        gUpperBoundChrom = gVertices;
    }
    if (gEdges != edges_processed)
    {
        std::cerr << "Invalid input file\n";
        return;
    }
    
    in_file.close();
}

void jp_algorithm(std::vector<int>& range, std::vector<int> vertex_offsets, std::vector<int> processator_graph)
{
    int number_vertices_per_processator, remainder_vertices_per_processator;
    int current_weight, number_of_colors, min_color;
    int first_vertex;

    bool is_current_weight_max = false;

    std::vector<int> current_colors = std::vector<int>(range[rank]);

    int* neighbour_colors = nullptr;
    //std::vector<int> neighbour_colors = std::vector<int>();

    number_vertices_per_processator = gVertices / number_of_nodes;

    for (int i = 0; i < gUpperBoundChrom; i++)
    {
        remainder_vertices_per_processator = (gVertices + rank) % number_of_nodes;
        first_vertex = number_vertices_per_processator * rank + remainder_vertices_per_processator * (remainder_vertices_per_processator < rank);

        // for each vertex of node
        for (int currentVertex = 0; currentVertex < range[rank]; currentVertex++)
        {
            current_weight = gWeights[first_vertex + currentVertex];
            is_current_weight_max = true;

            neighbour_colors = (int*)malloc(sizeof(int) * gVertices);
            memset(neighbour_colors, 0, sizeof(int) * gVertices);
            //neighbour_colors.resize(gVertices, 0);

            number_of_colors = 0;

            // go through neighbours, check if current weight max, gather neigh colors
            for (int neighbour = 0; neighbour < gVertices; neighbour++)
            {
                if (1 == processator_graph[currentVertex * gVertices + neighbour])
                {
                    // if neighbour colored
                    if (0 != gColors[neighbour])
                    {
                        neighbour_colors[number_of_colors++] = gColors[neighbour];
                    }

                    else if (current_weight < gWeights[neighbour]
                        || (current_weight == gWeights[neighbour] && neighbour > currentVertex))
                    {
                        is_current_weight_max = false;
                        break;
                    }
                }
            }

            if (is_current_weight_max == true && gColors[first_vertex + currentVertex] == 0)
            {
                //std::sort(neighbour_colors.begin(), neighbour_colors.end(), compare);
                std::qsort(neighbour_colors, number_of_colors, sizeof(int), compare2);

                // if neighbours uncolored or smallest color gt 1
                if (0 == number_of_colors || neighbour_colors[0] > 1)
                {
                    min_color = 1;
                }
                else
                {
                    for (int k = 0; k < gVertices; k++)
                    {
                        // check for gap in color array
                        if (k < gVertices - 1 && (neighbour_colors[k + 1] - neighbour_colors[k] > 1))
                        {
                            min_color = neighbour_colors[k] + 1;
                            break;
                        }
                        // if last, increment previous
                        else
                        {
                            min_color = neighbour_colors[number_of_colors - 1] + 1;
                        }
                    }
                }
                current_colors[currentVertex] = min_color;
            }
            if (NULL != neighbour_colors)
            {
                free(neighbour_colors);
            }
            
        }
        // each node sends its vertices' colors
        MPI_Gatherv(&current_colors[0], range[rank], MPI_INT, 
            &gColors[0], &range[0], &vertex_offsets[0], MPI_INT, 
            CUSTOM_MPI_RANK_ROOT, MPI_COMM_WORLD);

        // sync colors
        MPI_Bcast(&gColors[0], gVertices, MPI_INT, CUSTOM_MPI_RANK_ROOT, MPI_COMM_WORLD);
    }
}

int main(int argc, char** argv)
{
    std::string in_file, out_file;
    int number_vertices_per_processator, remainder_vertices_per_processator;
    int first_vertex, last_vertex;
    std::vector<int> range = std::vector<int>();
    std::vector<int> processator_graph;// = std::vector<int>();
    std::vector<int> processator_graph_size = std::vector<int>();
    std::vector<int> offsets = std::vector<int>();
    std::vector<int> vertex_offsets = std::vector<int>();
    double start_time, end_time, runtime, largest_runtime;

    MPI_Init(&argc, &argv);

    if (argc != 3)
    {
        std::cerr << "Invalid input: \nUsage: ./exe <path_to_input_file> <path_to_output_file>\n";
        MPI_Finalize();
        return 0;
    }

    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &number_of_nodes);

    if (rank == CUSTOM_MPI_RANK_ROOT)
    {
        in_file = argv[1];
        out_file = argv[2];
        read_graph_from_file(in_file);
    }

    MPI_Bcast(&gVertices, 1, MPI_INT, CUSTOM_MPI_RANK_ROOT, MPI_COMM_WORLD);
    MPI_Bcast(&gEdges, 1, MPI_INT, CUSTOM_MPI_RANK_ROOT, MPI_COMM_WORLD);
    MPI_Bcast(&gUpperBoundChrom, 1, MPI_INT, CUSTOM_MPI_RANK_ROOT, MPI_COMM_WORLD);

    
    processator_graph_size = std::vector<int>(number_of_nodes);
    offsets = std::vector<int>(number_of_nodes);
    vertex_offsets = std::vector<int>(number_of_nodes);
    range = std::vector<int>(number_of_nodes);

    // distribute (gVertices+rank)/number_of_nodes vertices per processator
    number_vertices_per_processator = gVertices / number_of_nodes;

    for (int i = 0; i < number_of_nodes; i++)
    {
        remainder_vertices_per_processator = (gVertices + i) % number_of_nodes;

        first_vertex = number_vertices_per_processator * i + remainder_vertices_per_processator * (remainder_vertices_per_processator < i);
        last_vertex = (i + 1) * number_vertices_per_processator + (remainder_vertices_per_processator + 1) * (remainder_vertices_per_processator < i) - 1;

        range[i] = last_vertex - first_vertex + 1;
        processator_graph_size[i] = range[i] * gVertices;

        offsets[0] = 0;
        vertex_offsets[0] = 0;

        if (i > 0)
        {
            offsets[i] = offsets[i - 1] + processator_graph_size[i - 1];
            vertex_offsets[i] = vertex_offsets[i - 1] + range[i - 1];
        }
    }

    processator_graph = std::vector<int>(processator_graph_size[rank]);
    //processator_graph.resize(processator_graph_size[rank], 0);

    MPI_Scatterv(&gGraph[0], &processator_graph_size[0], &offsets[0], MPI_INT, 
        &processator_graph[0], processator_graph_size[rank], MPI_INT, 
        CUSTOM_MPI_RANK_ROOT, MPI_COMM_WORLD);

    start_time = MPI_Wtime();

    gWeights = std::vector<int>(gVertices);

    if (CUSTOM_MPI_RANK_ROOT == rank)
    {
        for (int i = 0; i < gVertices; i++)
        {
            gWeights[i] = rand() % (gVertices * 1000);
        }
    }

    MPI_Bcast(&gWeights[0], gVertices, MPI_INT, CUSTOM_MPI_RANK_ROOT, MPI_COMM_WORLD);

    gColors = std::vector<int>(gVertices);

    jp_algorithm(range, vertex_offsets, processator_graph);

    end_time = MPI_Wtime();
    runtime = end_time - start_time;

    MPI_Allreduce(&runtime, &largest_runtime, 1, MPI_DOUBLE, MPI_MAX, MPI_COMM_WORLD);

    if (CUSTOM_MPI_RANK_ROOT == rank)
    {
        std::cout << largest_runtime << "\n";
        write_output(out_file);
    }

    MPI_Finalize();
    //system("sleep");
    return 0;
}