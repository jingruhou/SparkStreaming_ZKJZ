package zkjz.hjr.SQL

import org.apache.spark.{SparkContext, SparkConf}
/**
  * Created by Administrator on 2016/6/4.
  */
object zkjz4 {
  //结果集
  private var result =""
  private var Iterator = ()

  private val resultMQ = collection.mutable.Queue[String]()

  def main(args:Array[String]): Unit ={
    // 0 初始化配置
    val conf = new SparkConf().setAppName("ZKJZ_SQL").setMaster("local[*]")
    //val conf = new SparkConf().setAppName("ZKJZ_SQL").setMaster("spark://10.2.8.11:7077")
    //val conf = new SparkConf().setAppName("ZKJZ_SQL")
    val sc = new SparkContext(conf)

    // 2 创建RDD
    // /user/hive/warehouse/test/diag0530
    // /user/hive/warehouse/test/words
    //val outclinical_diago_rdd = sc.textFile("hdfs://10.2.8.11:8020/user/hive/warehouse/out/p*")
    //val outclinical_words_rdd = sc.textFile("hdfs://10.2.8.11:8020/user/hive/warehouse/door.db/outclinical_words/p*")

    //val outclinical_diago_rdd = sc.textFile("hdfs://10.2.8.11:8020/user/hive/warehouse/word/p*")
    //val outclinical_words_rdd = sc.textFile("hdfs://10.2.8.11:8020/user/hive/warehouse/words/p*")

    // val outclinical_diago_rdd = sc.textFile("hdfs://192.168.13.130:8020/user/houjr/sql/outclinical_diago530.txt")
    //val outclinical_words_rdd = sc.textFile("hdfs://192.168.13.130:8020/user/houjr/sql/outclinical_words.txt")

    val outclinical_diago_rdd = sc.textFile("E://StreamingData//sql//outclinical_diago530.txt")
    val outclinical_words_rdd = sc.textFile("E://StreamingData//sql//outclinical_words.txt")


    //outclinical_diago_rdd.foreach(println)

    //将数据切分为行数据
    val counts_word = outclinical_words_rdd.toArray()

    //k-v
    var diag = ""
    var words =""
    var map = Map(diag -> words)

    for(i <- 0 to counts_word.length-1){
      var line = counts_word(i)
      //println("#######################"+i+line+"######################")
      diag = line.split("\t")(0)
      words = line.split("\t")(1)
      map += (diag -> words)
    }
    println("####################Successful##########################")

    //将outclinical_diago_rdd数据集转化为数组缓冲
    var counts = outclinical_diago_rdd.toArray()

    //（1）这个rdd的长度（先collect，只有collect才会执行spark计算，算出这个rdd的长度）
    val counts_lenght = outclinical_diago_rdd.collect.length
    val counts_count = outclinical_diago_rdd.count
    println("outclinical_diago_rdd 的行数为： "+counts_count)

    //采用Map的方式实现---易悠
   /* val persons = sc.textFile("file:////home/pengyucheng/java/rdd2dfram.txt").map(row => {
    val attrs = row.split(",")
    val person = new Person
    person.setId(Integer.valueOf(attrs(0)))
    person.setAge(Integer.valueOf(attrs(2)))
    person.setName(attrs(1))
    person
    } )*/
    val outclinical = outclinical_diago_rdd.map(line =>{
      //var line = counts(i)
      var l = line.split("\t")(3)
      var s = line.split("\t")(0) + "\\t" + line.split("\t")(1) + "\\t" + line.split("\t")(2) + "\\t"
      var m = l.length
      while (m >= 1) {
        var j = 0
        while (j < l.length() - m + 1) {
          var s3 = l.substring(j, j + m)
          if (map.contains(s3)) {
            s += map(s3) + "."
            l = l.replace(s3, "")
          }
          j = j + 1
        }
        m = m - 1
      }
      resultMQ += s
    })


    //（2）怎么循环这个rdd，拿到这个rdd的行编号
    /*(0 to counts_lenght - 1).foreach { case i => {
      var line = counts(i)
      //var line = counts(i)
      var l = line.split("\t")(3)
      var s = line.split("\t")(0) + "\\t" + line.split("\t")(1) + "\\t" + line.split("\t")(2) + "\\t"
      var m = l.length
      while (m >= 1) {
        var j = 0
        while (j < l.length() - m + 1) {
          var s3 = l.substring(j, j + m)
          if (map.contains(s3)) {
            s += map(s3) + "."
            l = l.replace(s3, "")
          }
          j = j + 1
        }
        m = m - 1
      }
      resultMQ += s
      }
    }*/
    // resultMQ.foreach(println)
    val resultRDD = sc.parallelize(resultMQ)
    resultRDD.repartition(1).saveAsTextFile("E://StreamingData//sql//test")
    //resultRDD.repartition(1).saveAsTextFile("hdfs://10.2.8.11:8020/user/hive/warehouse/test/results/cxm")
    //resultRDD.repartition(1).saveAsTextFile("hdfs://10.2.8.11:8020/user/hive/warehouse/result")
    //resultRDD.repartition(1).saveAsTextFile("hdfs://192.168.13.130:8020/user/houjr/sql/result")

    sc.stop()
  }
}
