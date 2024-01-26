/* File:     hello.c
 *
 * Purpose:  A parallel hello, world program that uses OpenMP that
 *           achieved ordered output.
 *
 * Compile:  gcc -g -Wall -fopenmp -o hello hello.c
 * Run:      ./hello <number of threads>
 * 
 * Input:    none
 * Output:   A message from each thread in order
 * 
 * Author:   Gia Bao Hoang
 * Title:    PDC-23-S1-ASSIGN3-PART1
 * Created:  20/05/2023
 */
#include <stdio.h>
#include <stdlib.h>
#include <omp.h>   

/* Define constant values for the program */ 
// This is the maximum number of thread
#define MAX_THREADS 8

// This is the maximum length for greetings
#define MAX_STRING_LENGTH 50

void Hello(char buffer[][MAX_STRING_LENGTH]);  /* Thread function */

/*--------------------------------------------------------------------*/
int main(int argc, char* argv[]) {
   /* Get number of threads from command line */
   int thread_count = strtol(argv[1], NULL, 10);

   /* Initialize array to hold greetings */
   char buffer[MAX_THREADS][MAX_STRING_LENGTH];

   /* -Start of parallel region. 
      -The 'omp parallel' directive creates a team of threads (thread_count) and 
      begins the parallel region of the code.
      -The number of threads is set to the value obtained from the command line.
   */
   #pragma omp parallel num_threads(thread_count) 
   {
      /* -Single thread prints number of threads. 
         -The 'omp single' directive ensures that this block of code is 
         executed by only one thread, regardless of how many are available 
         in the team. 
      */
      #pragma omp single
      printf("Number of threads = %d\n", thread_count);

      /* -Each thread constructs its greeting.
         -The 'omp for' directive is used here to distribute the loop iterations
         across the threads in the team.
         -Since the number of threads matches the number of iterations, each 
         thread will only make one call to the Hello() function, to 
         construct its own greetings.
      */
      #pragma omp for
      for(int i = 0; i < thread_count; i++) {
         Hello(buffer);
      }
   } /* End of parallel region */

   /* Print greetings in order */
   for(int i = 0; i < thread_count; i++) {
      printf("%s", buffer[i]);
   }

   return 0; 
}  /* main */

/*-------------------------------------------------------------------
 * Function:    Hello
 * Purpose:     Thread function that constructs greeting and stores it in buffer.
 * In args:     buffer: 2D array for storing greetings from all threads.
 */
void Hello(char buffer[][MAX_STRING_LENGTH]) {
   /* Get the current thread rank and the total number of threads*/
   int my_rank = omp_get_thread_num();
   int thread_count = omp_get_num_threads();

   /* Construct greeting string */
   sprintf(buffer[my_rank], "Hello from thread %d of %d\n", my_rank, thread_count);   

}  /* Hello */
