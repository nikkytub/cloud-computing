import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.java.functions.FunctionAnnotation.ForwardedFields
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.scala._

import scala.collection.JavaConverters._

object CellCluster {
  def main(args: Array[String]) {

    // read and validate input paramters
    val params = ParameterTool.fromArgs(args)
    validateParamters(params)

    // create execution context
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

    // pre-process data
    val filteredInputData = filterInputData(params, env)
    val points = getPointsDataSet(filteredInputData)
    val centroids = getCentroidDataSet(params, filteredInputData)

    // calculate k-means
    val finalCentroids = centroids.iterate(params.getInt("iterations", 10)) { currentCentroids =>
      val newCentroids = points
        .map(new SelectNearestCenter).withBroadcastSet(currentCentroids, "centroids")
        .map { x => (x._1, x._2, 1L) }.withForwardedFields("_1; _2")
        .groupBy(0)
        .reduce { (p1, p2) => (p1._1, p1._2.add(p2._2), p1._3 + p2._3) }.withForwardedFields("_1")
        .map { x => new Centroid(x._1, x._2.div(x._3)) }.withForwardedFields("_1->id")
      newCentroids
    }

    val clusteredPoints: DataSet[(Int, Point)] =
      points.map(new SelectNearestCenter).withBroadcastSet(finalCentroids, "centroids")

    // writing output
    if (params.has("output")) {
      clusteredPoints.writeAsCsv(params.get("output"), "\n", ",", writeMode = FileSystem.WriteMode.OVERWRITE)
      env.execute("KMeans OpenCell Data")
    } else {
      println("Printing result to stdout. Use --output to specify output path.")
      clusteredPoints.print()
    }
  }

  //###########################
  //##### UTILITY METHODS #####
  //###########################

  def validateParamters(params: ParameterTool): Unit = {

    if (!params.has("input")) {
      System.err.println("No input file specified. \n" +
        "Please run 'CellCluster --input <input-path> \n" +
        "--iterations <value>' \n" +
        "--mnc <value>' \n" +
        "--k <value>' \n" +
        "--output <output-path>' \n")
      return
    }

    if (!params.has("iterations")) {
      System.out.println("No iterations specified. Default value will be used")
    }

    if (!params.has("mnc")) {
      System.out.println("No Mobile Network Codes specified. Default value will be used")
    }

    if (params.has("mnc")) {
      val mnc : Array[Int] = try {
        params.get("mnc").split(",").map( x => x.trim().toInt)
      } catch {
        case e: Exception => {
          System.err.println("Value for 'mnc' should be comma seperated integers")
          return
        }
      }
    }

    if (!params.has("k")) {
      System.out.println("No k specified. Default value will be used")
    }

    if (!params.has("output")) {
      System.out.println("No ouptut location specified. Results will be written to stdout")
    }

  }

  def filterInputData(params: ParameterTool, env: ExecutionEnvironment): DataSet[InputSchema] = {
    var filteredData: DataSet[InputSchema] = null

    val inputData = env.readCsvFile[InputSchema](
      filePath = params.get("input"),
      lineDelimiter = "\n",
      fieldDelimiter = ",",
      ignoreFirstLine = true,
      includedFields = Array(0,2,4,6,7)
    )

    // ignore LTE records
    filteredData = inputData.filter( !_.radio.equals("LTE") )

    // filter for specified mnc values
    if (params.has("mnc")) {
      val mnc: Array[Int] = params.get("mnc").split(",").map( x => x.trim().toInt)
      filteredData = filteredData.filter( x => mnc.contains(x.net) )
    }

    filteredData
  }

  def getPointsDataSet(filteredData: DataSet[InputSchema]): DataSet[Point] = {

    // return a dataset only relevant points
    filteredData.map(x => Point(x.lat, x.lon))
  }

  def getCentroidDataSet(params: ParameterTool, filteredData: DataSet[InputSchema]): DataSet[Centroid] = {

    var centroidData: DataSet[Centroid] = null

    val distinctCellIdsData = filteredData.distinct(2)

    val numDistinctCellIds = distinctCellIdsData.count()

    if (params.has("k")) {

      val numClusters: Int = params.getInt("k")

      if (numDistinctCellIds > numClusters) {

        centroidData = distinctCellIdsData
          .first(numClusters)
          .map(x => Centroid(x.cell, x.lat, x.lon))

        return centroidData
      }
    }

    return distinctCellIdsData.map( x => Centroid(x.cell, x.lat, x.lon))
  }


  //######################
  //##### DATA TYPES #####
  //######################

  case class InputSchema(radio: String, net: Int, cell: Int, lon: Double, lat: Double)

  // Common trait for operations supported by both points and centroids
  trait Coordinate extends Serializable {

    var x: Double
    var y: Double

    def add (other: Coordinate): this.type = {
      x += other.x
      y += other.y
      this
    }

    def div (other: Long): this.type = {
      x /= other
      y /= other
      this
    }

    def euclideanDistance (other: Coordinate): Double = {
      Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
    }

    def clear (): Unit = {
      x = 0
      y = 0
    }

    override def toString: String = {
      s"$x $y"
    }
  }

  // A simple two-dimensional point
  case class Point(var x: Double = 0, var y: Double = 0) extends Coordinate {

    override def toString: String = {
      s"$x,$y"
    }
  }

  // A simple two-dimensional centroid, basically a point with an ID
  case class Centroid(var id: Int = 0, var x: Double = 0, var y: Double = 0) extends Coordinate {

    def this (id: Int, p: Point) {
      this (id, p.x, p.y)
    }

    override def toString: String = {
      s"$id ${super.toString}"
    }
  }

  // Determines the closest cluster center for a data point
  @ForwardedFields(Array("*->_2"))
  final class SelectNearestCenter extends RichMapFunction[Point, (Int, Point)] {
    private var centroids: Traversable[Centroid] = null

    override def open (parameters: Configuration): Unit = {
      centroids = getRuntimeContext.getBroadcastVariable[Centroid]("centroids").asScala
    }

    def map(p: Point): (Int, Point) = {
      var minDistance: Double = Double.MaxValue
      var closestCentroidId: Int = -1
      for (centroid <- centroids) {
        val distance = p.euclideanDistance(centroid)
        if (distance < minDistance) {
          minDistance = distance
          closestCentroidId = centroid.id
        }
      }
      (closestCentroidId, p)
    }
  }
}
