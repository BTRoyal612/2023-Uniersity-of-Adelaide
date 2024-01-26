/* OS 2023, Cruz's Tutorial 3 - Question 3 illustration */
/* Compile with: 
	 gcc -o tut3.3 Tutorial3.q3.c -lpthread
     ./tut3.3
*/

#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include <assert.h>
#include <stdlib.h>
#include <time.h>

#define ANSI_COLOR_RED     "\x1b[31m"
#define ANSI_COLOR_GREEN   "\x1b[32m"
#define ANSI_COLOR_RESET   "\x1b[0m"


// Declaration of thread condition variable
pthread_cond_t In_CV = PTHREAD_COND_INITIALIZER;
pthread_cond_t Out_CV = PTHREAD_COND_INITIALIZER;

// declaring mutex
pthread_mutex_t M = PTHREAD_MUTEX_INITIALIZER;
 
int in = 0;
int out = 0; 
int n = 10; 
char b[10] = {'-','-','-','-','-','-','-','-','-','-'};

void *producer() { 

	while (1) { 
		usleep(rand()/10000);

		pthread_mutex_lock(&M); 
		while ((in + 1) % n == out){ 
			printf("\t\t\tProducer is about to wait %i, %i\n", in, out);
			pthread_cond_wait(&Out_CV, &M); 
			printf("\t\t\tProducer finished waiting %i, %i\n", in, out);
			// how long is this wait ?? 
		}

		b[in] = '^'; // for illustration show the newly inserted element as "^"
    	printf(ANSI_COLOR_RED "produce @%i:\t[ ", in);
		for(int p=0; p<n; p++) 
		   printf("%c", b[p] );
		printf(" ]\n" ANSI_COLOR_RESET); 
		b[in] = '*'; // afterwards the buffer has an element.


		in = (in + 1) % n; 
		pthread_cond_signal (&In_CV); 
		pthread_mutex_unlock (&M); 
	} 
}

void *consumer() {

	while (1) { 
		int w = 0;
		usleep(rand()/10000);

		pthread_mutex_lock (&M); 
		while (in == out) { 
			printf("\t\tConsumer is about to wait %i, %i\n", in, out);
		    pthread_cond_wait(&In_CV, &M); 
			printf("\t\tConsumer finished waiting %i, %i\n", in, out);
		}

		w = b[out];
		b[out]='v'; // just for illustration show the element to be consumed as "v"
		printf(ANSI_COLOR_GREEN "consume @%i:\t[ ", in);
		for(int p=0; p<n; p++) 
		   printf("%c", b[p] );
		printf(" ]\n" ANSI_COLOR_RESET); 
		b[out]='-'; // just for illustration -- "empty slot" afterwards


	 	out = (out + 1) % n; 
		pthread_mutex_unlock (&M); 
		pthread_cond_signal (&Out_CV); 

		/*consume item w */ 
	} 
}

int main(){
    pthread_t tid1, tid2;
 
    // Create threads
    pthread_create(&tid1, NULL, &producer, NULL); 
    pthread_create(&tid2, NULL, &consumer, NULL);

    pthread_join(tid1, NULL);
    pthread_join(tid2, NULL);
 
    return 0;
}
