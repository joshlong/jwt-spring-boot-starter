#!/usr/bin/env bash
username=user
password=password
token=$(curl -u $username:$password -X POST http://localhost:8080/token)
curl -v -H"Authorization: Bearer $token" http://localhost:8080/greetings

