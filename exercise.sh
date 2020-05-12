#!/usr/bin/env bash
username=username
password=password
response=$(curl  -u user:password -X POST http://localhost:8080/token  )
curl -H"Authorization: Bearer $response" http://localhost:8080/greetings
