#!/bin/sh

GENSIZE=$1
TESTSIZE=$2
DIR=ext/PracRand

$DIR/RNG_output sfc64 $GENSIZE | $DIR/rng-test.sh $TESTSIZE

