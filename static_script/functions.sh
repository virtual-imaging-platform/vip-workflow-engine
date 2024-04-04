#!/bin/bash

# Specify the path to your Bash script
script_file="Static_script.sh"


# Check if the script file exists
if [ ! -f "$script_file" ]; then
    echo "Error: Script file not found!"
    exit 1
fi

# Extract function names
function_names=$(grep -E '^\s*function\s+[a-zA-Z_][a-zA-Z0-9_]*\s*\(?' "$script_file" | awk '{print $2}' | tr -d '()')

# Array to store the order of function calls
function_call_order=()

# Iterate over the function names
for function_name in $function_names; do
    # Search for function calls in the script file
    function_calls=$(grep -E '\b'$function_name'\b' "$script_file" | grep -v '^\s*function\s')
    echo "$function_name"	

done
