package com.ddom.utils

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.jdbc.{JdbcDialect, JdbcDialects}
import org.apache.hadoop.fs._

object SparkUtils {

  def createSparkSession(appName:String, enableHiveSupport:Boolean): SparkSession = {
    if(enableHiveSupport) {
      SparkSession.builder()
        .appName(appName)
        .enableHiveSupport()
        .getOrCreate()
    }
    else {
      SparkSession.builder()
        .appName(appName)
        .getOrCreate()
    }
  }

  def setSparkConfigs(sparkSession:SparkSession, confMap:scala.collection.Map[String,String] = Map()): Unit = {
    val confIt = confMap.toIterator
    while(confIt.hasNext) {
      val t = confIt.next()
      sparkSession.conf.set(t._1, t._2)
    }
  }

  def readFromMSAccess(sparkSession:SparkSession, jdbcUrl:String, tableToRead:String, opts:scala.collection.Map[String,String] = Map()): DataFrame = {
    // A dedicated JDBC dialect needs to be defined for UcanaccessDriver in Spark
    object UcanaccessDialect extends JdbcDialect {
      override def canHandle(url: String): Boolean = url.toLowerCase(java.util.Locale.ROOT).startsWith("jdbc:ucanaccess")
      override def quoteIdentifier(colName: String): String = s"`$colName`"
    }
    JdbcDialects.registerDialect(UcanaccessDialect)

    if(opts.contains("query")) {
      sparkSession.sqlContext.read
        .format("jdbc")
        .option("url", jdbcUrl)
        .options(opts)
        .option("driver", "net.ucanaccess.jdbc.UcanaccessDriver")
        .load()
    }
    else {
      sparkSession.sqlContext.read
        .format("jdbc")
        .option("url", jdbcUrl)
        .option("dbtable", tableToRead)
        .options(opts)
        .option("driver", "net.ucanaccess.jdbc.UcanaccessDriver")
        .load()
    }
  }

  def writeToCsv(sparkSession:SparkSession, df:DataFrame, outputPath:String, write_mode:String = "error", opts:scala.collection.Map[String,String] = Map(), singleFile:Boolean = false): Unit = {
    if(singleFile) {
      var csvPath = outputPath
      if(!csvPath.endsWith(".csv"))
        csvPath += ".csv"

      val tmpCsvFolder = outputPath + "_tmp"
      df.coalesce(1)
        .write
        .format("com.databricks.spark.csv")
        .mode("overwrite")
        .options(opts)
        .save(tmpCsvFolder)

      val fs = FileSystem.get(sparkSession.sparkContext.hadoopConfiguration)
      val file = fs.globStatus(new Path(tmpCsvFolder + "/part*.csv"))(0).getPath.getName
      if(fs.exists(new Path(csvPath)))
        fs.delete(new Path(csvPath), true)
      fs.rename(new Path(tmpCsvFolder + "/" + file), new Path(csvPath))
      fs.delete(new Path(tmpCsvFolder), true)
    }
    else {
      df.write
        .format("com.databricks.spark.csv")
        .mode(write_mode)
        .options(opts)
        .save(outputPath)
    }
  }
}
