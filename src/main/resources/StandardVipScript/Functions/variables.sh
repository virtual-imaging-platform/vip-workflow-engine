startLog application_environment
echo "$variables"

# Iterate through each variable in $variables and export them
# Assuming $variables is a string containing key-value pairs separated by space
for variable in $variables; do
    key=$(echo "$variable" | cut -d'=' -f1)
    value=$(echo "$variable" | cut -d'=' -f2)
    export "$key"="$value"
done

# Stop log for application environment
stopLog application_environment