/*
MPI program for a set of tasks communicating in a ring topology, including left circular shift and printing ordered value of each task

Author: Gia Bao Hoang
Title: PDC-23-S1-ASSIGN1
Created: 31/03/2023
*/ 

#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <time.h>

int main()
{
    // Initialize the MPI environment
    MPI_Init(NULL, NULL);

    // Find out the rank (unique ID) and size (total number of tasks) of the MPI_COMM_WORLD communicator
    // world_rank is the rank of the task
    // world_size is the total number of tasks
    int world_rank, world_size;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    // Set the seed for the random number generator based on the current time and world rank,
    // ensuring different seeds for different tasks
    srand(time(NULL) + world_rank * 10);

    // Set m-value for each task
    // m is a random integer between 10 and 20 that will be shifted around in the ring
    int m = rand() % 11 + 10;
    
    // Define right and left tasks for each task to implement the ring topology
    // right is the rank of the immediate right neigbour of the current task in the ring
    // left is the rank of the immediate left neigbour of the current task in the ring
    int right = (world_rank + 1) % world_size;
    int left = (world_rank - 1 + world_size) % world_size;

    // For loop to shift the m-values to the left based on the shift variable
    int shift = 3;
    for (int i = 0; i < shift; i++)
    {
        // Send the m value to the left task using MPI_Send and output the interaction
        /*
            destination task: the left neigbour task
            message tag: 0
            communicator: MPI_COMM_WORLD
        */
        printf("Task %d sending number %d to task %d.\n", world_rank, m, left);
        MPI_Send(&m, 1, MPI_INT, left, 0, MPI_COMM_WORLD);
        
        // Receive the m value from the right task using MPI_Recv and output the interaction
        /*
            source task: the right neigbour task
            message tag: 0
            communicator: MPI_COMM_WORLD
            status object: MPI_STATUS_IGNORE
        */
        MPI_Recv(&m, 1, MPI_INT, right, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        printf("Task %d received number %d from task %d.\n", world_rank, m, right);
    }

    // Assign task 0 to be the master
    // Use if-else clause to partition the work between master and workers
    int master = 0;
    if (world_rank == master)
    {
        // Master collect m-values from all worker tasks (remaining task), sort them, and output the final m-values in order
        printf("Final m-values after left circular shift:\n");
        for (int i = 0; i < world_size; i++) {
            if (i == 0) {
                // Task 0 directly outputs its m-value
                printf("Task %d: %d\n", i, m);
            } else {
                // Task 0 receives the m-values (store in m_received variable) from other tasks and outputs them
                /*
                    source task: task i
                    message tag: 1
                    communicator: MPI_COMM_WORLD
                    status object: MPI_STATUS_IGNORE
                */
                int m_received;
                MPI_Recv(&m_received, 1, MPI_INT, i, 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                printf("Task %d: %d\n", i, m_received);
            }
        }
    } else {
        // Workers send their m-values to master (Task 0) for output
        /*
            destination task: master (task 0)
            message tag: 1
            communicator: MPI_COMM_WORLD
            status object: MPI_STATUS_IGNORE
        */
		MPI_Send(&m, 1, MPI_INT, master, 1, MPI_COMM_WORLD);
	}
	
	// Finalize the MPI environment, releasing all resources associated with it
	MPI_Finalize();
	return 0;
}