#!/usr/bin/env bash

# For development testing reasons, we need to reassembly when we change the code
# sbt assembly

# CONFIGURATION SECTION
#######################

# Set the global configuration

rundir="`dirname "$0"`"
rundir="`cd "$rundir"; pwd`"
. "$rundir"/setenv.sh


# RUN SECTION
#############

spark-submit \
  --class "com.lunatech.webdata.cuisine.ImportData" \
  --name "CuisineDataImporter" \
  --master $SPARK_MASTER_URL \
  --conf spark.task.maxFailures=20 \
  --conf spark.driver.memory=$SPARK_DRIVER_MEMORY \
  --conf spark.executor.memory=$SPARK_EXECUTOR_MEMORY \
  --verbose \
  $JAR_FILE \
  app.input.file.training="$APP_INPUT_FILE_TRAINING" \
  app.input.file.test="$APP_INPUT_FILE_TEST" \
  app.output.file.predictions="$APP_OUTPUT_FILE_PREDICTIONS" \
  app.model.dir="$APP_MODEL_DIR"
