@echo off
title ErrorSimulator
color 3f
javac -d bin ./src/*.java ./src/core/*.java ./src/exceptions/*.java
java -classpath bin ErrorSimulator
pause
