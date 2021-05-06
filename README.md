***Usage with files and jars in HDFS (needs yarn):***

```
spark-submit --class com.ddom.access2csv.AccdbToCsv \
    --conf "spark.yarn.dist.jars=hdfs:///%LIBS_PATH%/ucanaccess-5.0.1.jar,hdfs:///%LIBS_PATH%/jackcess-3.0.1.jar,hdfs:///%LIBS_PATH%/hsqldb-2.5.0.jar" \
    --conf "spark.yarn.dist.files=hdfs:///%ACCESS_FILE_PATH%,hdfs:///%JSON_FILE_PATH%" \
    "hdfs:///%LAPP_PATH%/access2csv-1.0.0.jar" "%JSON%"
```

***Usage with files and jars in local FS:***

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

***Links:***

- http://ucanaccess.sourceforge.net/site.html

Spark version: 2.4.7