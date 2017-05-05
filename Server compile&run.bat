@echo off
title Server
color 3f
javac -d bin ./src/*.java
java -classpath bin Server
pause
