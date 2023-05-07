import requests

# Yamcs connection parameters
yamcs_url = "http://localhost:8090"
instance = "oresat0"
destination_entity_id = 2

# File transfer parameters
bucket_name = "test"
file_name = "test.txt"
port = 12345

username = "admin"
password = "admin"

# Construct the endpoint URL
url = f"{yamcs_url}/api/filetransfer/{instance}/cfdp/{destination_entity_id}/transfer"

# Set the payload
payload = {
    "sourcePath": f"/buckets/{bucket_name}/{file_name}",
    "destinationPath": file_name,
    "transmissionMode": "RELIABLE",
    "mykissServerPort": port,
}

# Send the request
response = requests.post(url, json=payload, auth=(username, password))

# Check the response
if response.status_code != 200:
    response.raise_for_status()

response_json = response.json()
print(response_json)
