#!/usr/bin/env bash
username=username
password=password
token=$(curl -u user:password -X POST http://localhost:8080/token)
curl -H"Authorization: Bearer $token" http://localhost:8080/greetings
