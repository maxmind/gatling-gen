#!/bin/sh

TESTSIZE=$1
DIR=ext/PracRand

$DIR/RNG_test stdin8 -tlmin $TESTSIZE -tlmax $TESTSIZE

