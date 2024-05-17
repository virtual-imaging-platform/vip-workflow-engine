#configurationJson=../../config/"${0%.sh}-configuration.json"
echo $0


filename=$(basename "${0%.sh}")
echo "Filename without extension: $filename"

# Create the new path
configurationJson="config/$filename-configuration.json"
echo "New configuration path: $configurationJson"





variables=(
    applicationName jsonFileName jobId invocationJson downloads invocationString envVariables parameters uploads
)

for variable in "${variables[@]}"; do
    # Get the value from jq
    value=$(jq -r ".jobConfiguration.$variable" "$configurationJson")
    # Assign the value to the variable using indirect reference
    eval "$variable=\"$value\""
done

# Print the values to verify
for variable in "${variables[@]}"; do
    echo "$variable: ${!variable}"
done