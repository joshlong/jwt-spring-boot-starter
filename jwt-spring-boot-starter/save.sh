#!/usr/bin/env bash

mvn spring-javaformat:apply && git commit -am "save $(date ) " && git push