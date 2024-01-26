# Perfdata Directory

The perfdata directory contains files related to recording trial runtimes for the nbody solvers program. This README file provides guidance on how to use the `automate.sh` script to automate the process of running the program and recording the elapsed time.

## Files

1. `automate.sh`: This shell script automates the execution of the nbody solvers program with different parameters and records the elapsed time. It uses a combination of parameters such as the number of threads, particles, and time steps to generate meaningful data. The results are saved in the `results.csv` file.

2. `performance_comparison.xlsx`: This Excel file is used to store the recorded trial runtimes of all four versions of the nbody solvers program. It serves as a convenient way to compare the performance of different versions.
    - `Trial Runtimes`: Record each trial runtimes.
    - `Comparison Runtimes`: Compare runtimes between parameters combination.
    - `Extracted Runtimes`: Extracted runtimes view for specific parameters.
    
## Parameters

The `automate.sh` script uses the following parameters for running the nbody solvers program:

- Number of threads: 1, 2, 4
- Number of particles: 10, 100, 1000
- Number of time steps: 10, 100, 1000, 10000

The script will generate trial runtimes for all possible combinations of these parameters.

## Usage

To use the `automate.sh` script and record trial runtimes, follow these steps:

1. Make sure you have the necessary dependencies and the nbody solvers program installed.

2. Open a terminal and navigate to the `perfdata` directory.

3. Run the following command to give execute permission to the `automate.sh` script:

```sh
chmod +x automate.sh
```

4. Run the following command to execute the `automate.sh` script:

```sh
./automate.sh
```

5. The script will run the nbody solvers program with different combinations of parameters, such as the number of threads, particles, and time steps. It will perform 10 trials for each combination.

6. The elapsed time for each trial will be recorded in the `results.csv` file.

7. After the script finishes running, you can open the `compare.xlsx` file to view and compare the recorded trial runtimes of all four versions of the nbody solvers program.

Feel free to adjust the parameters in the `automate.sh` script or modify the script according to your requirements.