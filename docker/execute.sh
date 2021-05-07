#!/bin/bash

cd /usr/src/app
cp docker/jars/libs/* ${FILES_LOCATION}
cp docker/input/* ${FILES_LOCATION}
cp access2csv/target/${SPARK_APPLICATION_JAR} ${FILES_LOCATION}

/spark/bin/spark-submit --class ${SPARK_APPLICATION_CLASS} \
    --driver-memory ${SPARK_DRIVER_MEMORY} --executor-memory ${SPARK_EXECUTOR_MEMORY} \
    --jars "${FILES_LOCATION}ucanaccess-5.0.1.jar,${FILES_LOCATION}jackcess-3.0.1.jar,${FILES_LOCATION}hsqldb-2.5.0.jar" \
    "${FILES_LOCATION}${SPARK_APPLICATION_JAR}" "${FILES_LOCATION}${SPARK_PARAMETERS_FILE}"

#sh /wait-for-step.sh
#sh /submit.sh