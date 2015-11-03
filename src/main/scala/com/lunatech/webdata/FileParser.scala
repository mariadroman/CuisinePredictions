import com.lunatech.webdata.{CustomLineInputFormat}
import org.apache.hadoop.io.{Text, LongWritable}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Sparkles {
  val dataPath: String = "ML/train.json"

  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("Sparkles").setMaster("local").registerKryoClasses(Array(Class.forName("org.apache.hadoop.io.LongWritable"),Class.forName("org.apache.hadoop.io.Text")))
    val sc = new SparkContext(conf)
    sc.textFile(dataPath)
    val rawData: RDD[(LongWritable, Text)] =
      sc.newAPIHadoopFile[LongWritable, Text, CustomLineInputFormat](dataPath)
    rawData.saveAsTextFile("ML/test.txt")
    rawData.foreach(println)
  }
}
