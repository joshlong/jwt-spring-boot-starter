#!/usr/bin/env bash
username=client1
password=pw
response=$(curl -i -X POST http://localhost:8080/api/login\?username\=${username}\&password\=${password} | grep Authorization | cut -f3 -d" "  )
echo "RESPONE: $response "
curl -H"Authorization: Bearer $response" http://localhost:8080/hello
