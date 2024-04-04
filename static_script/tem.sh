#!/bin/bash

# Function to print hashes with function name engraved
print_design() {
    local function_name="$1"
    local num_hashes=40  # Adjust the number of hashes as needed
    local padding=$(( (num_hashes - ${#function_name}) / 2 ))

    for ((i = 0; i < 4; i++)); do
        printf "%0.s#" $(seq 1 $num_hashes)
        printf "\n"
    done

    # Print function name inside the hashes with spaces
    printf "%0.s " $(seq 1 $padding)
    printf "%s" "$function_name"
    printf " %0.s" $(seq 1 $padding)
    printf "\n"

    for ((i = 0; i < 4; i++)); do
        printf "%0.s#" $(seq 1 $num_hashes)
        printf "\n"
    done
}

# Print design for the "cleanup" function
print_design "cleanup"
