#!/bin/bash

export CLASSPATH=$(hadoop classpath)
export HADOOP_CLASSPATH=$CLASSPATH

rm -rf ./../OUT
hadoop jar ./../target/eventrecommendationengine-1.0-SNAPSHOT.jar mapreduce.preparedata.UserEventValueMatrixDriver ./../DATA ./../OUT