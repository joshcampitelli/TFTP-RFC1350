@echo off
title Client
color 3f
javac -d bin ./src/*.java
java -classpath bin Client
pause
