/**
  * Created by Srikanth on 3/16/2017.
  */

package com.cloudwick.spark.program

import java.util.Calendar

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.expressions.WeekOfYear
import org.apache.spark.{SparkConf, SparkContext}

object airport extends App {

  val sparkConf=new SparkConf().setMaster("local").setAppName("spark")
  val sc=SparkContext.getOrCreate(sparkConf)
  val file1 = sc.textFile("C:/Users/Srikanth/Desktop/BTS Dataset/Ontime/*")

  var filteredRDD = file1.first()
  var NoHeaderRDD = file1.filter(x=> !x.contains(filteredRDD))
  var filesRDD = NoHeaderRDD

  case class airport_data(Year: Int, Month: Int,DayofMonth: Int,DayOfWeek: Int, weekOfYear: Int,DepTime: String,
                           CRSDepTime: Int,ArrTime: String,CRSArrTime: Int,UniqueCarrier: String,FlightNum: Int,
                           TailNum: String,ActualElapsedTime: String,CRSElapsedTime: String,AirTime: String,
                           ArrDelay: String,DepDelay: String,Origin: String,Dest: String,Distance: Int)

  val dataRDD = filesRDD.map { line =>
    val cols = line.split(",")
    val week = week_of_year(cols(0).toInt, cols(1).toInt-1, cols(2).toInt)
    airport_data(cols(0).toInt, cols(1).toInt, cols(2).toInt, cols(3).toInt, week,cols(4), cols(5).toInt, cols(6), cols(7).toInt, cols(8), cols(9).toInt, cols(10), cols(11),cols(12), cols(13), cols(14), cols(15), cols(16), cols(17), cols(18).toInt)
  }

  val depRDD = dataRDD.filter(x=> (x.Origin=="SFO" && x.DepDelay!="NA" && x.DepDelay.toInt>0))
  val arrRDD = dataRDD.filter(x=> (x.Dest=="SFO" && x.ArrDelay!="NA"&& x.ArrDelay.toInt>0))

  val depmap = depRDD.map(x=>((x.Year,x.weekOfYear,x.UniqueCarrier),(x.DepDelay.toInt )))
  val arrmap = arrRDD.map(x=>((x.Year,x.weekOfYear,x.UniqueCarrier),(x.ArrDelay.toInt )))

  val depred = depmap.reduceByKey(_+_).sortByKey()
  val arrred = arrmap.reduceByKey(_+_).sortByKey()

  val unionRDD = depred.union(arrred).reduceByKey(_+_).sortByKey()
  unionRDD foreach println

  val file = "C:/Users/Srikanth/Desktop/BTS Dataset/output/weekly_report.txt"
  unionRDD.saveAsTextFile(file)

  def week_of_year(year:Int, month: Int, date:Int): Int ={
    val cal: Calendar = Calendar.getInstance()
    cal.set(year,month,date)
    cal.get(Calendar.WEEK_OF_YEAR)
  }

  //  val file2 = sc.textFile("C:/Users/Srikanth/Desktop/BTS Dataset/Ontime/2006.csv")
  // val file3 = sc.textFile("C:/Users/Srikanth/Desktop/BTS Dataset/Ontime/2007.csv")
  // val file4 = sc.textFile("C:/Users/Srikanth/Desktop/BTS Dataset/Ontime/2008.csv")
  // val it = Iterator(file2,file3,file4)
  /*
    val fr = dataRDD.toDF()
    fr.registerTempTable("DATA")
    sqlContext.sql("select distinct(Year) from DATA").show()  */

  /* val sqlContext = new SQLContext(sc)
  import sqlContext.implicits._ */

 /* val sqlContext = new SQLContext(sc)
  import sqlContext.implicits._
  val dataDF = dataRDD.toDF()
  dataDF.show()
    dataDF.registerTempTable("DATA2005")
  sqlContext.sql("SELECT Year, weekOfYear, SUM(DepDelay) UniqueCarrier from DATA2005 where Origin='SFO' and DepDelay>0 group by Year ").show()
  //sqlContext.sql("SELECT SUM(DELAY) AS AIRPORTDELAY, weekOfYear, UniqueCarrier from(SELECT UniqueCarrier, Year, Month, DayofMonth, DayOfWeek, weekOfYear, ArrDelay AS DELAY FROM DATA2005 where (Dest='SFO') AND ArrDelay>0 UNION ALL SELECT UniqueCarrier, Year, Month, DayofMonth, DayOfWeek, weekOfYear, DepDelay AS DELAY FROM DATA2005 where (Origin='SFO') AND DepDelay>0) x GROUP BY UniqueCarrier").show()
*/

  /*while(it.hasNext){
     val RDD = it.next()
     filteredRDD = RDD.first()
     NoHeaderRDD = RDD.filter(x=> !x.contains(filteredRDD))
     NoHeaderRDD.toDF().show()
     filesRDD.union(NoHeaderRDD)
 }  */

}
