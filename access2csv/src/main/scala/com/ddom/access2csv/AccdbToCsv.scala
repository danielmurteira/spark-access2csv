package com.ddom.access2csv

import com.ddom.utils.{JsonUtils, SparkUtils}
import org.slf4j.{Logger, LoggerFactory}

object AccdbToCsv {

  // JSON FIELDS
  private val INPUT_PATH  = "inputPath"
  private val INPUT_TABLE  = "inputTable"
  private val OUTPUT_PATH  = "outputPath"
  private val MODE  = "mode"
  private val OPTIONS  = "options"
  private val EXTRA  = "extra"
  private val SINGLE_FILE  = "singleFile"
  private val SEP_ASCII  = "sepAscii"
  private val SEP  = "sep"

  // DEFAULT VALUES
  private val DEFAULT_WRITE_MODE = "error"
  private val DEFAULT_SINGLE_FILE = false
  private val DEFAULT_SEP_ASCII = 44 // = ","

  // JSON OPTIONS ALLOWED
  private val _jdbc_read_options = List("query", "partitionColumn", "lowerBound", "upperBound", "numPartitions", "queryTimeout", "fetchsize", "sessionInitStatement", "customSchema", "pushDownPredicate", "keytab", "principal")
  private val _csv_write_options = List("sep", "quote", "escape", "charToEscapeQuoteEscaping", "escapeQuotes", "quoteAll", "header", "nullValue", "emptyValue", "encoding", "compression", "dateFormat", "timestampFormat", "ignoreLeadingWhiteSpace", "ignoreTrailingWhiteSpace", "lineSep")
  private val _csv_write_mode = List("append", "overwrite", "ignore", "error", "errorifexists")

  // CLASS SPECIFICS
  private val APP_NAME = "AccdbToCsv"
  private val URL_START = "jdbc:ucanaccess://"
  private val URL_END = ";memory=false"

  case class AccdbToCsvArgs(spark:Option[Map[String,String]], accdb:Map[String,Any], csv:Map[String,Any])

  // either a json file or a json string can be received in the 1st argument
  def getArgsToClass(args: Array[String]): AccdbToCsvArgs = {
    if(args(0).startsWith("{") && args(0).endsWith("}"))
      JsonUtils.readFromString(args(0), classOf[AccdbToCsvArgs])
    else
      JsonUtils.readFromFile(args(0), classOf[AccdbToCsvArgs])
  }

  def parseJsonArgs(args: Array[String], logger: Logger): AccdbToCsvArgs = {
    try {
      val argsClass = getArgsToClass(args)
      val finalSpark:Map[String, String] = argsClass.spark.getOrElse(Map())
      val accdb:Map[String,Any] = argsClass.accdb
      val csv:Map[String,Any] = argsClass.csv

      val inputPath = accdb(INPUT_PATH).asInstanceOf[String]
      val inputTable = accdb(INPUT_TABLE).asInstanceOf[String]
      val inputOptions = accdb.getOrElse(OPTIONS, Map()).asInstanceOf[Map[String,String]]
      val filteredInputOptions = inputOptions.filter(t => _jdbc_read_options.contains(t._1))

      val outputPath = csv(OUTPUT_PATH).asInstanceOf[String]
      var outputMode = csv.getOrElse(MODE, DEFAULT_WRITE_MODE).asInstanceOf[String]
      if(!_csv_write_mode.contains(outputMode.toLowerCase))
        outputMode = DEFAULT_WRITE_MODE

      val outputOptions = csv.getOrElse(OPTIONS, Map()).asInstanceOf[Map[String,String]]
      var filteredOutputOptions = outputOptions.filter(t => _csv_write_options.contains(t._1))
      val outputExtra = csv.getOrElse(EXTRA, Map()).asInstanceOf[Map[String,Any]]
      val singleFile = outputExtra.getOrElse(SINGLE_FILE, DEFAULT_SINGLE_FILE).asInstanceOf[Boolean]
      val sepAscii = outputExtra.getOrElse(SEP_ASCII, DEFAULT_SEP_ASCII).asInstanceOf[Int]
      if(!outputOptions.contains(SEP))
        filteredOutputOptions = filteredOutputOptions + (SEP -> sepAscii.toChar.toString)

      val finalAccdb = Map(INPUT_PATH -> inputPath, INPUT_TABLE -> inputTable, OPTIONS -> filteredInputOptions)
      val finalCsv = Map(OUTPUT_PATH -> outputPath, MODE -> outputMode, OPTIONS -> filteredOutputOptions, SINGLE_FILE -> singleFile)
      AccdbToCsvArgs(Some(finalSpark), finalAccdb, finalCsv)
    }
    catch {
      case e: Exception =>
        logger.error("ParseJsonArgs: Error reading input parameters.")
        logger.error(e.printStackTrace().toString)
        sys.exit(2)
    }
  }

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(getClass)

    logger.info("Parsing arguments...")
    val argsClass = parseJsonArgs(args, logger)

    logger.info("Initializing conversion...")
    val spark = SparkUtils.createSparkSession(appName=APP_NAME,enableHiveSupport=false)
    SparkUtils.setSparkConfigs(spark, argsClass.spark.get)

    val jdbcUrl = URL_START + argsClass.accdb(INPUT_PATH).asInstanceOf[String] + URL_END
    val df = SparkUtils.readFromMSAccess(
      spark,
      jdbcUrl,
      argsClass.accdb(INPUT_TABLE).asInstanceOf[String],
      argsClass.accdb(OPTIONS).asInstanceOf[Map[String,String]]
    )

    logger.info("Printing schema from source...")
    df.printSchema()

    SparkUtils.writeToCsv(
      sparkSession = spark,
      df = df,
      outputPath = argsClass.csv(OUTPUT_PATH).asInstanceOf[String],
      write_mode = argsClass.csv(MODE).asInstanceOf[String],
      opts = argsClass.csv(OPTIONS).asInstanceOf[Map[String,String]],
      singleFile = argsClass.csv(SINGLE_FILE).asInstanceOf[Boolean]
    )

    logger.info("CSV created successfully!")
  }
}
