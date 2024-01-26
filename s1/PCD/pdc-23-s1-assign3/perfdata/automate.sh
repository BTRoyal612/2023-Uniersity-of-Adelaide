#!/bin/bash

# Program name to automate
program=./omp_nbody_red_all_cyclic

# Arrays for different parameters
threads=(1 2 4)
particles=(10 100 1000)
numb_timestep=(10 100 1000 10000)

# Print the CSV header
echo "threads,particles,numb_timestep,size_timestep" > results.csv

# Loop over all combinations
for t in ${threads[@]}; do
    for p in ${particles[@]}; do
        for nt in ${numb_timestep[@]}; do
            for i in {1..10}; do
                # Run the command and capture output
                result=$($program $t $p $nt 0.01 1 g)

                # Write the parameters and result to the CSV file
                echo "$t,$p,$nt,$result" >> results.csv
            done
        done
    done
done