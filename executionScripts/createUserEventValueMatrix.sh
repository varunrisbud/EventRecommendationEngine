#!/bin/bash

BASEDIR=/user/user01/EventRecommendationEngine
export CLASSPATH=$(hadoop classpath)
export HADOOP_CLASSPATH=$CLASSPATH

rm -rf $BASEDIR/OUT
hadoop jar $BASEDIR/target/eventrecommendationengine-1.0-SNAPSHOT.jar mapreduce.preparedata.UserEventValueMatrixDriver $BASEDIR/DATA $BASEDIR/OUT
