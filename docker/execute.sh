#!/bin/bash

cd /usr/src/app

cp docker/jars/libs/hsqldb-2.5.0.jar ${SPARK_APPLICATION_JAR_LOCATION}
cp docker/jars/libs/jackcess-3.0.1.jar ${SPARK_APPLICATION_JAR_LOCATION}
cp docker/jars/libs/ucanaccess-5.0.1.jar ${SPARK_APPLICATION_JAR_LOCATION}
cp docker/input/file.accdb ${SPARK_APPLICATION_JAR_LOCATION}
cp docker/input/params.json ${SPARK_APPLICATION_JAR_LOCATION}
cp access2csv/target/${SPARK_APPLICATION_JAR_NAME}.jar ${SPARK_APPLICATION_JAR_LOCATION}

#sh /wait-for-step.sh
#sh /submit.sh

/spark/bin/spark-submit --class com.ddom.access2csv.AccdbToCsv \
    --master "yarn" --deploy-mode "cluster" \
    --jars "/ucanaccess-5.0.1.jar,/jackcess-3.0.1.jar,/hsqldb-2.5.0.jar" \
    --files "/file.accdb,/params.json" \
    "/access2csv-1.0.0.jar" "/params.json"