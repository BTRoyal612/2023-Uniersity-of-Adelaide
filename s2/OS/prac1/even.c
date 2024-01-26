/*********************************************************************
Program : even.c
Version : 1.0
Author  : Gia Bao Hoang
Date    : 05/08/2023
--------------------------------------------------------------------
Description: This program takes a single command-line argument 'n'
             and prints the first 'n' even numbers. The program
             includes a delay after printing each number to allow
             for signal handling.
             
             Signals Handled:
             - SIGHUP: Prints "Ouch!" and continues.
             - SIGINT: Prints "Yeah!" and continues.

Usage: Compile the program using a C compiler and run it with a single
       integer argument. For example:
       
       gcc -o even even.c
       ./even 5

       To send signals, use the 'kill' command in another terminal.
       For example, to send SIGHUP to a process with PID 1234:
       
       kill -HUP 1234
---------------------------------------------------------------------
*********************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>

// Signal handler for SIGHUP
void handle_sighup(int signum) {
    printf("Ouch!\n");
    fflush(stdout);
}

// Signal handler for SIGINT
void handle_sigint(int signum) {
    printf("Yeah!\n");
    fflush(stdout);
}

int main(int argc, char *argv[]) {
    // Check the number of arguments
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <n>\n", argv[0]);
        return EXIT_FAILURE;
    }

    // Check for valid input n
    int n = atoi(argv[1]);
    if (n <= 0) {
        fprintf(stderr, "Please enter a positive integer for n.\n");
        return EXIT_FAILURE;
    }

    // Set up signal handlers
    signal(SIGHUP, handle_sighup);
    signal(SIGINT, handle_sigint);

    for (int i = 0; i < n; i++) {
        printf("%d\n", 2 * i);
        fflush(stdout); 
        sleep(5); 
    }

    return EXIT_SUCCESS;
}
