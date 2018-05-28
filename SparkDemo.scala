import okhttp3.ConnectionPool
import org.apache.calcite.avatica.ColumnMetaData.StructType
import org.apache.hive.hplsql.HplsqlParser.HiveContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext, types}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.flume.{FlumeUtils, SparkFlumeEvent}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.immutable.{HashMap, HashSet}

object SparkDemo {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Demo").setMaster("Local[1]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val checkpointDirectory = "hdfs://Master:9000/usr/checkpointDir"
    val textFileDir = "hdfs://Master:9000/usr/data"

    val topics = new HashMap[String, Int]()
    topics.+("SparkStreaming" -> 2)

    val kafkaParams = new HashMap[String, String]()
    kafkaParams.+("metadata.broker.list" -> "Master:2181, work1:2181, work2:2181")
    val topicsDirected = new HashSet[String]()
    topicsDirected.+("SparkStreamingDirected")

    // log format songkun_iPhone_Apple
    val userClickLogDStream = ssc.socketTextStream("Master", 9999)
//    val lines = ssc.textFileStream(textFileDir)
//    val lines = FlumeUtils.createStream(ssc, "Master", 9999)
//    val lines = FlumeUtils.createPollingStream(ssc, "Master", 9999)
//    val lines = KafkaUtils.createStream(ssc, kafkaParams, topics, StorageLevel.MEMORY_AND_DISK_SER_2)
//    val lines = KafkaUtils.createDirectStream(ssc, kafkaParams, topicsDirected)

    /*
    val words = lines.flatMap(line => line.split(" "))
    val pairs = words.map(word => (word, 1))
    val count = pairs.reduceByKey(_ + _)
    count.print()
    */

    val formattedUserClickLogDStream = userClickLogDStream
      .map(log => (log.split("_")(2) + "_" + log.split("_")(1), 1))

    val categoryUserClickLogDSteam = formattedUserClickLogDStream
      .reduceByKeyAndWindow(_ + _, _ - _, Seconds(60), Seconds(20))

    // distinct
    val distinctCategoryUserClickLogDSteam = categoryUserClickLogDSteam.transform(rdd => rdd.distinct())
    
    categoryUserClickLogDSteam.foreachRDD(rdd => {
      val categoryItemRow = rdd.map(categoryItem => {
        val category = categoryItem._1.split("_")(0)
        val item = categoryItem._1.split("_")(1)
        val click_count = categoryItem._2
        Row(category, item, click_count)
      })

      val structType = types.StructType(Array(
        StructField("category", StringType, true),
        StructField("item", StringType, true),
        StructField("click_count", IntegerType, true)
      ))
      val sqlContext = new SQLContext(rdd.sparkContext)
      val categoryItemDF = sqlContext.createDataFrame(categoryItemRow, structType)

      categoryItemDF.registerTempTable("categoryItemTable")

      val resultDF = sqlContext.sql(
        "select category, item, click_count from " +
          "(select category, item, click_count, row_number over " +
          "(partition by category order by clickCount desc) rank from categoryItemTable) subquery " +
          "where rank <= 3"
      )

      val resultRowRDD = resultDF.rdd

      resultRowRDD.foreachPartition(partitionOfRecords => {
        val connection = ConnectionPool.getConnection()
        partitionOfRecords.foreach(record => {
          val sql = "insert into categorytop3 (category, item, count) values ('" +
            record.getAs("category") + "','" + record.getAs("item") + "'," +
            record.getAs("click_count") + ")"
          val stmt = connection.createStatement()
          stmt.executeUpdate(sql)
        })
        ConnectionPool.returnConnection(connection)
      })
    })

    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }
}


