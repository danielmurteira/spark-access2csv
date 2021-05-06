package com.ddom.access2csv

import com.ddom.access2csv.AccdbToCsv.AccdbToCsvArgs
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfterEach
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.types.{DecimalType, DoubleType, IntegerType, LongType, StringType, StructField, StructType}
import net.ucanaccess.jdbc.UcanaccessSQLException
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.util.{Failure, Success, Try}
import com.ddom.utils.{JsonUtils, SparkUtils}

class AccdbToCsvTests extends AnyFunSuite with BeforeAndAfterEach {

  //private val TEST_RESOURCES_PATH = "access2csv/src/test/resources/" //starts at project root
  private val PARAMS_FILE = getClass.getResource("/params.json").getPath
  private val ACC_FILE = getClass.getResource("/sample.accdb").getPath
  private val ACC_URL = "jdbc:ucanaccess://" + ACC_FILE + ";memory=false"
  private val CSV_FILE = getClass.getResource("/").getPath + "output/sample.csv"
  private val CSV_FOLDER = getClass.getResource("/").getPath + "output/sample_folder"

  private val NON_EXISTING_TABLE = "NonExistingTable"
  private val EMPTY_TABLE = "EmptyTable"
  private val SAMPLE_TABLE = "SampleTable"
  private val SAMPLE_COL1 = "ID"
  private val SAMPLE_COL2 = "FirstName"
  private val SAMPLE_COL3 = "Email"
  private val SAMPLE_COL4 = "Phone"
  private val SAMPLE_COL5 = "Weight"

  private var sparkSession:SparkSession = _

  override def beforeEach() {
    sparkSession = SparkSession.builder().appName("AccdbToCsvTests")
      .master("local")
      .getOrCreate()
  }
  
  test("MS Access -> DF non existing table check"){
    assertThrows[UcanaccessSQLException] {
      SparkUtils.readFromMSAccess(sparkSession, ACC_URL, NON_EXISTING_TABLE)
    }
  }
  
  test("MS Access -> DF empty table check"){
    val df = SparkUtils.readFromMSAccess(sparkSession, ACC_URL, EMPTY_TABLE)
    assert(df.count() == 0)
  }
  
  test("MS Access -> DF sample table content check"){
    val df = SparkUtils.readFromMSAccess(sparkSession, ACC_URL, SAMPLE_TABLE)
    assert(df.except(dfModel()).count() == 0)
  }

  test("MS Access -> DF sample table schema check"){
    val df = SparkUtils.readFromMSAccess(sparkSession, ACC_URL, SAMPLE_TABLE)
    val it = df.schema.toIterator
    while (it.hasNext) {
      val t = it.next()
      t.name match {
        case SAMPLE_COL1 => assert(t.dataType.equals(IntegerType))
        case SAMPLE_COL2 => assert(t.dataType.equals(StringType))
        case SAMPLE_COL3 => assert(t.dataType.equals(StringType))
        case SAMPLE_COL4 => assert(t.dataType.equals(LongType))
        case SAMPLE_COL5 => assert(t.dataType.equals(DoubleType))
      }
    }
  }

  test("MS Access -> DF sample table custom schema check"){
    val options = Map("customSchema" -> "Phone int, Weight decimal(10,4)")
    val df = SparkUtils.readFromMSAccess(sparkSession, ACC_URL, SAMPLE_TABLE, options)
    val it = df.schema.toIterator
    while (it.hasNext) {
      val t = it.next()
      t.name match {
        case SAMPLE_COL1 => assert(t.dataType.equals(IntegerType))
        case SAMPLE_COL2 => assert(t.dataType.equals(StringType))
        case SAMPLE_COL3 => assert(t.dataType.equals(StringType))
        case SAMPLE_COL4 => assert(t.dataType.equals(IntegerType))
        case SAMPLE_COL5 => assert(t.dataType.equals(DecimalType(10,4)))
      }
    }
  }

  test("DF -> CSV folder check"){
    var succeeded = false
    Try {
      SparkUtils.writeToCsv(sparkSession, dfModel(), CSV_FOLDER, "overwrite")
    } match {
      case Success(_) => succeeded = true
      case Failure(e) => None
    }
    assert(succeeded)

    val fs = getFS
    assert(fs.exists(new Path(CSV_FOLDER)))
  }

  test("DF -> CSV single file check"){
    var succeeded = false
    Try {
      SparkUtils.writeToCsv(sparkSession, dfModel(), CSV_FILE, "overwrite", singleFile=true)
    } match {
      case Success(_) => succeeded = true
      case Failure(e) => None
    }
    assert(succeeded)

    val fs = getFS
    assert(fs.exists(new Path(CSV_FILE)))
  }

  test("JSON string parsing check"){
    val args = JsonUtils.readFromString("{ \"accdb\": { }, \"csv\": { } }", classOf[AccdbToCsvArgs])
    assert(args.spark.isEmpty)
    assert(args.accdb.isEmpty)
    assert(args.csv.isEmpty)
  }

  test("JSON file parsing check"){
    val args = JsonUtils.readFromFile(PARAMS_FILE, classOf[AccdbToCsvArgs])
    assert(args.spark.isDefined)
    assert(args.accdb.nonEmpty)
    assert(args.csv.nonEmpty)
    assert(args.spark.get("spark.app.name").equals("ConversionApp"))
    assert(args.accdb("inputPath").equals("access2csv/src/test/resources/sample.accdb"))
    assert(args.csv("mode").equals("overwrite"))
  }
  
  override def afterEach() {
    sparkSession.stop()
  }

  def dfModel(): DataFrame = {
    val schema = StructType(Array(
      StructField(SAMPLE_COL1, IntegerType),
      StructField(SAMPLE_COL2, StringType),
      StructField(SAMPLE_COL3, StringType),
      StructField(SAMPLE_COL4, LongType),
      StructField(SAMPLE_COL5, DoubleType)
    ))

    val data = sparkSession.sparkContext.parallelize(Seq(
      Row(1, "John", "john@accdb.com", 12345678.asInstanceOf[Long], 180.0),
      Row(2, "Mary", "mary@accdb.com", 123456789.asInstanceOf[Long], 125.3),
      Row(3, "Andy", "andy@accdb.com", 1234567890.asInstanceOf[Long], 150.0945)
    ))

    sparkSession.createDataFrame(data, schema)
  }

  def getFS: FileSystem = {
    val conf = sparkSession.sparkContext.hadoopConfiguration
    FileSystem.get(conf)
  }
}
