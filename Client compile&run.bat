@echo off
title Client
color 3f
javac -d bin ./src/com/tftp/*.java ./src/com/tftp/core/*.java ./src/com/tftp/io/*.java ./src/com/tftp/exceptions/*.java ./src/com/tftp/simulation/*.java ./src/com/tftp/workers/*.java
java -classpath bin com.tftp.Client
pause
