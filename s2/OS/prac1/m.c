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

/* shell prompt */
void prompt(void) {
    fflush(stdout);
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
                if (!background) {
                    // Wait for child to finish
                    wpid = wait(0);
                    if (wpid < 0) {
                        perror("wait"); 
                    }
                }
                break;
        }
    }
    return 0;
}