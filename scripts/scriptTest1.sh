#!/bin/bash

ant clean
ant compile jar

echo "Test 1"

java -jar mapreduce.jar 1 test1.txt outputTest1.txt
cat /dev/null > outputTest1.txt
java -jar mapreduce.jar 2 test1.txt outputTest1.txt
cat /dev/null > outputTest1.txt
java -jar mapreduce.jar 4 test1.txt outputTest1.txt
cat /dev/null > outputTest1.txt
java -jar mapreduce.jar 8 test1.txt outputTest1.txt
cat /dev/null > outputTest1.txt



