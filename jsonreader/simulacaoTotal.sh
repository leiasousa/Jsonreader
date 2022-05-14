#!/bin/bash
###########################################################
#Shell script for sequenced execution of the ExecuteONS.jar, JsonReader.jar, and GnuplotONS.jar programs.
#Author: Lucas Rodrigues Costa
#Objective: To simulate ONS
###########################################################

scenario=USA

rm -rf results$scenario
java -jar ExecuteONS.jar -t 12 -j ons.jar -L 50 700 50 -d results$scenario -ra KSP SPV -s 1 2 3 4 5 -f xml/$scenario\_PI.xml -e lucasrc.rodri@gmail.com

rm -rf finalResults$scenario
mkdir finalResults$scenario

ra=KSP
java -jar JsonReader.jar resultsUSA/$ra\_seeds $ra
mv resultsUSA/$ra\_seeds/$ra.txt finalResults$scenario

ra=SPV
java -jar JsonReader.jar resultsUSA/$ra\_seeds $ra
mv resultsUSA/$ra\_seeds/$ra.txt finalResults$scenario

cp GnuplotONS.jar finalResults$scenario
cd finalResults$scenario
java -jar GnuplotONS.jar KSP SPV
rm GnuplotONS.jar
cd ..
