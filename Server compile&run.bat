@echo off
title Server
color 3f
javac -d bin ./src/*.java ./src/core/*.java ./src/io/*.java ./src/exceptions/*.java
java -classpath bin Server
pause
