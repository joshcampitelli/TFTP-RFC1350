@echo off
title Client
color 3f
javac -d bin ./src/*.java ./src/core/*.java ./src/io/*.java ./src/exceptions/*.java
java -classpath bin Client
pause
