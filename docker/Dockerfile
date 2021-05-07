FROM bde2020/spark-submit:2.4.5-hadoop2.7

LABEL maintainer="Daniel Murteira <domurteira@gmail.com>"

ENV SPARK_APPLICATION_JAR access2csv-1.0.0.jar
ENV SPARK_APPLICATION_CLASS com.ddom.access2csv.AccdbToCsv
ENV SPARK_DRIVER_MEMORY 2g
ENV SPARK_EXECUTOR_MEMORY 2g
ENV SPARK_PARAMETERS_FILE params.json
ENV FILES_LOCATION /

COPY docker/execute.sh /

RUN apk add --no-cache openjdk8 maven
RUN chmod +x /execute.sh
RUN mkdir -p /usr/src/app

COPY . /usr/src/app
RUN cd /usr/src/app && mvn dependency:resolve
RUN cd /usr/src/app && mvn clean package

CMD ["/bin/bash", "/execute.sh"]