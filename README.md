# Azure Address Allocator
This Java program automates the creation, allocation and implemention into the current Azure VM of new public IP addresses - after allocating a new IP address, the virtual machine will be able to communicate using that new address.
This could be useful for developers who need to test things such as rate limiting, virtual networks or crawler scripts.

## Features (being implemented)
* Creation/deletion of new public IP addresses
* Full-duplex use of new IP addresses as programs can bind to the private IP of each public IP
* User-oriented API system allowing users with keys to interact via HTTP/POST
* Automated (API endpoint) SOCKS 4/5 proxy creation using [java-socks-proxy-server](https://github.com/bbottema/java-socks-proxy-server) (included in source)
* Support for Ubuntu 18.04+ (netplan)

## Creating API keys
Only the loopback address (localhost/127.0.0.1) is allowed to create/delete API keys for users. To achieve this, a curl command can be used to create users with the desired username(s):
```
# For a server listening on port 8080:
curl --request POST --data "[\"username\"]" localhost:8080/register
```
The server will then respond with:
```
{"status":"success","message":"1 users registered."}
```
if the command completed successfully, or
```
{"status":"error","message":"One or more users are already registered. No users have been registered."}
```
if the user is already registered.
