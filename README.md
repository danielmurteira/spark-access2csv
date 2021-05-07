###**Summary:**

Spark module to convert MS Access files to CSV format.

###**Usage:**

***Files and jars in HDFS (needs yarn):***

```
spark-submit --class com.ddom.access2csv.AccdbToCsv \
    --conf "spark.yarn.dist.jars=hdfs:///%LIBS_PATH%/ucanaccess-5.0.1.jar,hdfs:///%LIBS_PATH%/jackcess-3.0.1.jar,hdfs:///%LIBS_PATH%/hsqldb-2.5.0.jar" \
    --conf "spark.yarn.dist.files=hdfs:///%ACCESS_FILE_PATH%,hdfs:///%JSON_FILE_PATH%" \
    "hdfs:///%LAPP_PATH%/access2csv-1.0.0.jar" "%JSON%"
```

***Files and jars in local FS:***

```
spark-submit --class com.ddom.access2csv.AccdbToCsv \
    --jars "%LIBS_PATH%/ucanaccess-5.0.1.jar,%LIBS_PATH%/jackcess-3.0.1.jar,%LIBS_PATH%/hsqldb-2.5.0.jar" \
    --files "%ACCESS_FILE_PATH%,%JSON_FILE_PATH%" \
    "%LAPP_PATH%/access2csv-1.0.0.jar" "%JSON%"
```

***Minimum required structure for the input json parameter:***

```
{
    "accdb": {
        "inputPath": "example.accdb",
        "inputTable": "exampletable"
    },
    "csv": {
        "outputPath": "/path/to/example.csv"
    }
}
```

You can find a more extensive example written [here](https://github.com/danielmurteira/spark-access2csv/blob/master/access2csv/src/main/resources/example_params.json). All the available options are listed below.

***Spark parameters:***

Parameter | Optional | Default Value
------------ | ------------- | -------------
spark | yes | [Docs](https://spark.apache.org/docs/latest/configuration.html)

***Access parameters:***

Parameter | Optional | Default Value
------------ | ------------- | -------------
accdb | no | { }
inputPath | no | -
inputTable | no | -
options | yes | [Docs](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html)

***CSV parameters:***

Parameter | Optional | Default Value
------------ | ------------- | -------------
csv | no | { }
outputPath | no | -
mode | yes | [Docs](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/DataFrameWriter.html#mode-java.lang.String-)
options | yes | [Docs](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/DataFrameWriter.html#csv-java.lang.String-)
extra | yes | { }
extra.singleFile | yes | false
extra.sepAscii | yes | 44

***Notes:***

- accdb.options and csv.options values need to be defined as strings
- csv.options.sep and csv.extra.sepAscii do the same thing, with priority given to the first if both are defined
- csv.mode will default to "overwrite" if csv.extra.singleFile is set to true
- logs will print the data schema (column types received from MS Access)
- the application works both with a json file or a json string for the input parameter

###**Docker support:**

You can also run the application if you have docker installed.
- Download the repository:
```
git clone https://github.com/danielmurteira/spark-access2csv.git
```
- Place the MS Access file to be converted in docker/input
- Edit params.json in docker/input with your file and table names
- Create the docker image:
```
docker build -t dmurteira/spark-access2csv -f docker/Dockerfile .
```
- Run the container:
```
docker run --name spark-access2csv -v %cd%/docker/output:/results  -d dmurteira/spark-access2csv
```
- The csv file will appear in docker/output

###**Other info:**

***Links:***
- http://ucanaccess.sourceforge.net/site.html
- https://github.com/big-data-europe/docker-spark

***Versions:***
- Spark 2.4.5
- Scala 2.11.12
- Java 1.8
- UCanAccess 5.0.1
