/*********************************************************************
   Program : minishell.c                   
   Version : 1.4
   Author  : Gia Bao Hoang
   Date    : 05/08/2023
 --------------------------------------------------------------------
   Description:
   minishell is a simple implementation of a shell in C. It provides 
   the basic functionalities of a shell, such as command execution, 
   changing directories, and handling background processes.

   How to compile:
   gcc minishell.c -o minishell
   This will produce an executable named 'minishell'.

   To run:
   ./minishell
 --------------------------------------------------------------------
   File			: minishell.c
   Compiler/System	: gcc/linux
********************************************************************/
#include <sys/types.h>
#include <sys/wait.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <signal.h>

#define NV 20 /* max number of command tokens */
#define NL 100 /* input buffer size */
char line[NL]; /* command input buffer */

// Structure to store background process information
typedef struct {
    int pid;         // PID of the background process
    int id;          // ID of the background process
    char cmd[NL];    // Command that was run
    int finished;    // Flag to indicate if the process has finished
} BgProcess;

BgProcess bg_processes[NV]; // Storage for background processes
int bg_count = 0;           // Counter for background processes
int bg_index = 1;           // Index for labeling background processes

/* shell prompt */
void prompt(void) {
    fflush(stdout);
}

// Signal handler: This function gets called when a child process ends
void child_exit_handler(int signum)
{
    int status;
    int wpid = waitpid(-1, &status, WNOHANG);

    // Keep checking for more child processes that may have ended
    while (wpid > 0) {
        // Update the 'finished' status of the reaped child process in our bg_processes array
        for (int i = 0; i < bg_count; i++) {
            if (bg_processes[i].pid == wpid)
            {
                bg_processes[i].finished = 1;
                break;
            }
        }
        wpid = waitpid(-1, &status, WNOHANG);
    }
}

/* argk - number of arguments */
/* argv - argument vector from command line */
/* envp - environment pointer */
int main(int argk, char *argv[], char *envp[]) {
    
    int frkRtnVal;          /* value returned by fork sys call */
    int wpid;               /* value returned by wait */
    char *v[NV];            /* array of pointers to command line tokens */
    char *sep = " \t\n";    /* command line token separators */
    int i;                  /* parse index */

    // Register the signal handler for child exit signal
    signal(SIGCHLD, child_exit_handler);

    /* prompt for and process one command line at a time */
    while (1) {
        prompt();
        fgets(line, NL, stdin);
        fflush(stdin);

        // Handle end of file
        if (feof(stdin)) { 
            exit(0);
        }

        // Ignore comments and blank lines
        if (line[0] == '#' || line[0] == '\n' || line[0] == '\000')
            continue; 
        
        // Tokenize the command line input
        v[0] = strtok(line, sep);
        for (i = 1; i < NV; i++) {
            v[i] = strtok(NULL, sep);
            if (v[i] == NULL)
                break;
        }
        
        // Handle background commands
        int background = 0;
        if (v[i - 1] && strcmp(v[i - 1], "&") == 0) {
            v[i - 1] = NULL;
            background = 1;
        }

        // Handle 'cd' command
        if (strcmp(v[0], "cd") == 0) {
            if (chdir(v[1]) != 0) {
                perror("chdir"); 
            }
            continue; 
        }

        /* fork a child process to exec the command in v[0] */
        switch (frkRtnVal = fork()) {
            /* fork returns error to parent process */
            case -1: 
                perror("fork"); 
                break;
            /* code executed only by child process */
            case 0: 
                if (execvp(v[0], v) == -1) {
                    perror("execvp"); 
                    exit(EXIT_FAILURE); 
                }
                break;
            /* code executed only by parent process */
            default: 
                // Run background process
                if (background) {
                    // Register background process
                    bg_processes[bg_count].pid = frkRtnVal;
                    bg_processes[bg_count].id = bg_index; 
                    bg_processes[bg_count].finished = 0;

                    // Construct command without &
                    char buffer[NL] = {0};
                    for (int j = 0; j < i - 1; j++) { 
                        strcat(buffer, v[j]);  
                        strcat(buffer, " ");   
                    }
                    strncpy(bg_processes[bg_count].cmd, buffer, NL - 1); 
                    bg_processes[bg_count].cmd[NL - 1] = '\0'; 
                                    
                    fprintf(stdout, "[%d] %d\n", bg_processes[bg_count].id, frkRtnVal);

                    bg_count++;
                    bg_index++;
                    background = 0;
                } else {
                    // Wait for child to finish
                    wpid = wait(0);
                    if (wpid < 0) {
                        perror("wait"); 
                    }
                }
                break;
        }

        // Report and cleanup completed background processes
        for (int i = 0; i < bg_count; i++) {
            if (bg_processes[i].finished) {
                fprintf(stdout,"[%d]+   Done                    %s\n", bg_processes[i].id, bg_processes[i].cmd);
                
                // Remove the process from the array
                for (int j = i; j < bg_count - 1; j++) {
                    bg_processes[j] = bg_processes[j+1];
                }
                bg_count--;
                i--;  
            }
        }  

        // Reset the background index if no processes are running in the background
        if (bg_count == 0) {
            bg_index = 1;
        }      
    }
    return 0;
}