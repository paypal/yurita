/**
 * Copyright 2019 PayPal Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paypal.risk.platform.veda.analytics.clustering

import java.sql.Timestamp

import com.paypal.risk.platform.veda.analytics.Implicits._
import com.paypal.risk.platform.veda.analytics.anomaly.window.Window
import org.apache.spark.SparkConf
import org.apache.spark.sql._

object ClusteringExampleApp extends App {

  // create SparkSession
  val appName = "AnomalyDetectionAPI"
  val sparkConf = new SparkConf().setAppName(appName).setMaster("local[*]")
  // .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  val spark = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  // for implicit conversions like converting RDDs to DataFrames
  import spark.implicits._

  // create Dataset of some data type
  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")

  def getTimestamp(window: (java.util.Date, java.util.Date)): Timestamp = {
    new Timestamp(window._1.getTime + scala.util.Random.nextInt((window._2.getTime - window._1.getTime).toInt))
  }

  val window1 = (dateFormat.parse("2011-01-18 01:00:00.0"), dateFormat.parse("2011-01-18 01:00:10.0"))
  val window2 = (dateFormat.parse("2011-01-18 02:00:10.1"), dateFormat.parse("2011-01-18 02:00:20.0"))
  val window3 = (dateFormat.parse("2011-01-18 03:00:20.1"), dateFormat.parse("2011-01-18 03:00:30.0"))

  case class Person(name: String, surname: String, age: Int, income: Double, gender: String, luck: Array[Double],
    timestamp: Timestamp)

  val inputDF: DataFrame =
    Seq(
      Person("Ned", "Stark", 40, 40.6, "M", Array(5.5), getTimestamp(window1)),
      Person("Arya", "Stark", 9, 40.1, "F", Array(5.6), getTimestamp(window2)),
      Person("Sansa", "Stark", 13, 46.3, "F", Array(5.6), getTimestamp(window3)),
      Person("Jon Snow", "Stark", 17, 11.4, "M", Array(12.4), getTimestamp(window1)),
      Person("Catelyn", "Stark", 35, 67.5, "F", Array(5.5), getTimestamp(window2)),
      Person("Joffrey", "Baratheon", 87, 35.5, "M", Array(5.6), getTimestamp(window3)),
      Person("Myrcella", "Baratheon", 36, 4.4, "F", Array(8.7), getTimestamp(window1)),
      Person("Robert", "Baratheon", 40, 13.1, "M", Array(5.6), getTimestamp(window2)),
      Person("Balon", "Greyjoy", 20, 89.9, "M", Array(5.6), getTimestamp(window3)),
      Person("Theon", "Greyjoy", 19, 11.1, "M", Array(1.2), getTimestamp(window1)),
      Person("Yara", "Greyjoy", 20, 12.5, "F", Array(8.8), getTimestamp(window2)),
      Person("Tyrion", "Lannister", 30, 77.8, "M", Array(5.6), getTimestamp(window3)),
      Person("Jaime", "Lannister", 35, 40.3, "M", Array(8.9), getTimestamp(window1)),
      Person("Cersei", "Lannister", 35, 45.1, "F", Array(8.7), getTimestamp(window2)),
      Person("Daenerys", "Targaryen", 15, 990.6, "F", Array(13.6), getTimestamp(window3)),
      Person("Viserys", "Targaryen", 23, 23.2, "M", Array(5.4), getTimestamp(window1))
    ).toDF()

  val df = inputDF

  val dbscanPipe = ClusteringPipelineBuilder()
    .onColumns(Seq("luck"))
    .setWindowing(Window.fixed("1 hour"))
    .buildDensityClusteringModel(ClusteringAlgorithms.DBSCAN(2, 2))

  val hdbscanPipe = ClusteringPipelineBuilder()
    .onColumns(Seq("luck"))
    .setWindowing(Window.fixed("1 hour"))
    .buildDensityClusteringModel(ClusteringAlgorithms.HDBSCAN(2, 2))


  // compose the workload to process
  val workload = ClusteringWorkloadBuilder()
    .addAllPipelines(dbscanPipe ++ hdbscanPipe)
    .buildWithWatermark("timestamp", "2 hours")

  // dataset extended api
  df.cluster(workload).map(_.toString).foreach(println(_))
}

