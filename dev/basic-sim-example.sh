#!/bin/sh

PORT=$1

SIM="com.maxmind.gatling.simulation.BasicSimulationExample"
JAR="target/scala-2.11/gatlinggen_2.11-0.1-SNAPSHOT.jar"
DIR="sim-results"
NAME="basic-1"
SIMDIR="$DIR/$NAME"

G="gatling"

JAVA_CLASSPATH=$JAR JAVA_OPTS="-Dport=$PORT" $G -on "$DIR"  \
    -rf "$DIR" -bdf "$DIR" -s "$SIM"

