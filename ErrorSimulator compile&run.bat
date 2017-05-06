@echo off
title ErrorSimulator
color 3f
javac -d bin ./src/*.java
java -classpath bin ErrorSimulator
pause
