@echo off
title IntermediateHost
color 3f
javac -d bin ./src/*.java
java -classpath bin IntermediateHost
pause
