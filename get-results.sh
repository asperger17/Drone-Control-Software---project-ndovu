#!/usr/bin/env bash

NDOVU_JAR_FILE_PATH="${PWD}/target/ndovu-1.0-SNAPSHOT.jar"
MONTH="03"
YEAR="2021"
LATITUDE="55.944425"
LONGITUDE="-3.188396"
SEED="5678"
PORT="80"

for i in {1..19}; do
  java -jar $NDOVU_JAR_FILE_PATH $(printf "%02d" $i) $MONTH $YEAR $LATITUDE $LONGITUDE $SEED $PORT
done

for i in {21..25}; do
  java -jar $NDOVU_JAR_FILE_PATH $(printf "%02d" $i) $MONTH $YEAR $LATITUDE $LONGITUDE $SEED $PORT
done
