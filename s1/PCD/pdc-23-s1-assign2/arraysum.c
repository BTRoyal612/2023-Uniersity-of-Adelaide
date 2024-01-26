/*
 * This program initializes an array of size 1000 and calculates the sum of the array, sequentially and in parallel.
 * 
 * Author: Gia Bao Hoang
 * Title: PDC-23-S1-ASSIGN2-PART2
 * Created: 07/05/2023
 */ 
#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

// Define the static size of the required array
#define ARRAY_SIZE 1000

int main(int argc, char *argv[]) {
    int arr[ARRAY_SIZE];
    int sequential_sum = 0;
    int parallel_sum = 0;

    // Get the number of threads from the command line
    int thread_count = strtol(argv[1], NULL, 10);

    // Step 1: Initialize the array sequentially
    // Iterate through the array and assign the index value to each element
    for (int i = 0; i < ARRAY_SIZE; i++) {
        arr[i] = i;
    }

    // Step 2: Write a sequential for loop to sum the elements of the array and print the sum
    // Iterate through the array and add the value of each element to the 'sequential_sum' variable
    for (int i = 0; i < ARRAY_SIZE; i++) {
        sequential_sum += arr[i];
    }
    printf("Sequential sum: %d\n", sequential_sum);

    // Step 3: Write a parallel for loop to initialize the array
    // The 'omp parallel for' directive is used to parallelize the initialization of the array
    // The loop iterations will be divided among the available threads
    #pragma omp parallel for num_threads(thread_count)
    for (int i = 0; i < ARRAY_SIZE; i++) {
        arr[i] = i;
    }

    // Step 4: Parallelize the summation loop using a parallel directive and a reduction clause
    // The 'omp parallel' directive is used with 'reduction' to parallelize the summation loop
    #pragma omp parallel num_threads(thread_count) reduction(+:parallel_sum)
    {
        // Get the current thread's rank
        int my_rank = omp_get_thread_num();
        // Get the total number of threads
        int num_threads = omp_get_num_threads();

        // Partition the summation task based on the current thread's rank
        // Calculate the start index and the end index of the current thread
        int local_start = my_rank * (ARRAY_SIZE / num_threads);
        int local_end = (my_rank + 1) * (ARRAY_SIZE / num_threads);

        // If the current thread is the last one, adjust the end index to cover the remaining elements
        if (my_rank == num_threads - 1) {
            local_end = ARRAY_SIZE;
        }

        // Iterate through the assigned portion of the array and accumulate the sum of elements
        // parallel_sum is the reduction variable with addition reduction operator
        for (int i = local_start; i < local_end; i++) {
            parallel_sum += arr[i];
        }
    }
    printf("Parallel sum: %d\n", parallel_sum);

    return 0;
}
