#!/usr/bin/env bash
# This script is used to extract the hostnames in the europeana.properties file, while creating the Kubernetes ingress
# for the deployment.
#
# Usage: ./extract-hosts.sh <path_to_europeana_properties>
# Exports two properties to the environment:
#     K8S_HOSTNAME: to be used as the main ingress hostname
#     K8S_SERVER_ALIASES: to be used as ingress server aliases
if [ ! -f $1 ]; then
  echo "'$1' cannot be found. Provide a valid path to europeana.properties file"
  exit 1
fi

# Concatenates arguments, using the first argument as a delimiter
# eg: join_by , (a b c) ->a,b,c
#
join_by() {
  local IFS="$1"
  shift
  echo "$*"
}

# First arg should contain path to europeana.properties file
EUROPEANA_PROPERTIES=$1

# Extracts all hostnames in the europeana.properties file, specified as routes for the application
# Regex matches the following patterns
#    route1.path = api,www,search-api,search-api-green
#    route2.path = metis-publish-api-prod
#
# Matches are concatenated in the HOSTNAMES_ALIAS variable. ie:
# HOSTNAMES_ALIAS = api,www,search-api,search-api-green,metis-publish-api-prod
HOSTNAMES_ALIAS=$(cat $EUROPEANA_PROPERTIES | gawk 'match($0, /^route[0-9]?\.path\s+=(.*)$/,a) {print a[1]}' | sed 's/\n/,/g')

# Extracts all hostnames in the europeana.properties file for which we don't want to create an ingress route for.
# These are typically routes that are handled by the API Gateway
# Regex matches the following patterns
#    route1.ingressIgnore = api
#
# Matches are concatenated in the HOSTNAMES_ALIAS variable. ie:
# EXCLUSIONS = api,www
EXCLUSIONS=$(cat $EUROPEANA_PROPERTIES | gawk 'match($0, /^route[0-9]?\.ingressIgnore\s+=(.*)$/,a) {print a[1]}' | sed 's/\n/,/g')

# Create an array containing both strings, so we can easily manipulate its contents
IFS=',' read -r -a hostname_array <<<"$HOSTNAMES_ALIAS"
IFS=',' read -r -a exclusion_array <<<"$EXCLUSIONS"

#echo "hostname count is ${#hostname_array[@]}"
#echo "hostname count is ${#hostname_array[@]}"
#echo "=${hostname_array[*]}"

# Delete every host in hostname_array, which exists in exclusion_array
for i in "${exclusion_array[@]}"; do
  hostname_array=("${hostname_array[@]/$i/}")
done

## The previous loop creates gaps in hostname_array (as elements in the middle could have been deleted).
## We close the gaps here
for i in "${!hostname_array[@]}"; do
  if [ -z "${hostname_array[i]}" ]; then
    # omit empty values from new array
    continue
  else
    new_array+=("${hostname_array[i]}")
  fi
done
hostname_array=("${new_array[@]}")
unset new_array

# We use the first element as the hostname for the ingress.
# All other elements are set as the server alias
last_index=$((${#hostname_array[@]} - 1))
server_aliases_array="${hostname_array[*]:1:$last_index}"

export K8S_HOSTNAME="${hostname_array[0]}"
export K8S_SERVER_ALIASES=$(join_by , $server_aliases_array)

echo "K8S_HOSTNAME: $K8S_HOSTNAME"
echo "K8S_SERVER_ALIASES: $K8S_SERVER_ALIASES"
