# Directory: `perfdata`
The `perfdata` directory contains important data related to the performance analysis of the parallel histogram program. This directory includes:

## Files Included:

1. **performance_recording.xlsx**: A spreadsheet containing the recorded runtimes for each trial, along with the calculated speedup and efficiency values. This file helps in tracking the performance of the parallel histogram program across multiple runs and configurations.

2. **input.txt**: A text file containing the necessary input parameters for running a trial of the parallel histogram program. The file consists of 4 lines, each containing a single number:

* Line 1: Number of bins
* Line 2: Minimum measurement
* Line 3: Maximum measurement
* Line 4: Dataset size
The `input.txt` file is used to provide consistent input parameters for each trial.

3. **output.txt**: A text file that stores the output of a trial run of the parallel histogram program. The output includes the printed histogram and the maximum elapsed time for that trial. We only consider the maximum elapsed time for recording in the `performance_recording.xlsx` file, as it represents the overall execution time determined by the slowest process in the parallel program.

## Performance Analysis Workflow:

1. Run the parallel histogram program with the desired configurations, using the `input.txt` file for providing input parameters.
2. Save the output to the `output.txt` file.
3. Update the `performance_recording.xlsx` file with the maximum elapsed time obtained from the `output.txt` file.

To execute the histogram.c and obtain the measured runtime, run the following commands in the terminal. Ensure that `input.txt` and `output.txt` are in the same directory as `histogram.c`:

```sh
$ mpicc -g -Wall histogram.c -o hist
$ mpiexec -n (number of processes) ./hist < input.txt > output.txt
