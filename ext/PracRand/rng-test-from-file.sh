#!/bin/sh

TESTSIZE=$1
DIR=ext/PracRand

$DIR/rng-test.sh $TESTSIZE < $DIR/random-bytes.tmp

