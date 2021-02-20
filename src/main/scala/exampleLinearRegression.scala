import ml.network.api._
import ml.network.api.given
import ml.tensors._
import ml.tensors.ops._

import scala.reflect.ClassTag
import scala.math.Numeric.Implicits._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Random, Using}
import java.io.{File,PrintWriter}
import scala.collection.parallel.CollectionConverters._

@main def linearRegression() =       
  val random = new Random(100)
  val weight = random.nextFloat()
  val bias = random.nextFloat()

  def batch(batchSize: Int): (ArrayBuffer[Double], ArrayBuffer[Double]) =
    val inputs = ArrayBuffer.empty[Double]
    val outputs = ArrayBuffer.empty[Double]
    def noise = random.nextDouble / 5
    (0 until batchSize).foldLeft(inputs, outputs) { case ((x, y), _) =>        
        val rnd = random.nextDouble
        x += rnd + noise
        y += bias + weight * rnd + noise
        (x, y)
    }

  val ann = Sequential[Double, SimpleGD](
    meanSquareError,
    learningRate = 0.00005f,    
    batchSize = 16,
    gradientClipping = clipByValue(5.0d)
  ).add(Dense())    

  val (xBatch, yBatch) = batch(10000)
  val x = Tensor1D(xBatch.toArray)
  val y = Tensor1D(yBatch.toArray)
  val ((xTrain, xTest), (yTrain, yTest)) = Tensor.split(0.2f, (x, y))

  val model = ann.train(xTrain.T, yTrain.T, epochs = 200)

  println(s"current weight: ${model.weights}")
  println(s"true weight: $weight")
  println(s"true bias: $bias")

  // Test Dataset
  val testPredicted = model.predict(xTest)  
  val value = meanSquareError[Double].apply(yTest.T, testPredicted)
  println(s"test meanSquareError = $value")

  //////////////////////////////////////////
  // Store all posible data for plotting ///
  //////////////////////////////////////////

  // datapoints
  val dataPoints = xBatch.zip(yBatch).map((x, y) => List(x.toString, y.toString))
  store("metrics/datapoints.csv", "x,y", dataPoints.toList)

  //Store loss metric into CSV file
  val lossData = model.history.losses.zipWithIndex.map((l,i) => List(i.toString, l.toString))
  store("metrics/lr.csv", "epoch,loss", lossData)

  //gradient
  val gradientData = model.history.weights.zip(model.history.losses)
      .map { (weights, l) => 
        weights.headOption.map(w => 
          List(w.w.as1D.data.head.toString, w.b.as1D.data.head.toString)
        ).toList.flatten :+ l.toString
      }
  store("metrics/gradient.csv", "w,b,loss", gradientData)

  // loss surface
  val weights = for (i <- 0 until 100) yield i/100d 
  val biases = weights
  
  println("Calculating loss surface")
  val losses = weights.par.map { w =>
    val wT = w.as2D
    biases.foldLeft(ArrayBuffer.empty[Double]) { (acc, b) =>
      val loss = ann.loss(x.T, y.T, List(Weight(wT, b.as1D)))  
      acc :+ loss
    }
  }
  println("Done calculating loss surface.")

  val metricsData = weights.zip(biases).zip(losses)
    .map{ case ((w, b), l) => List(w.toString, b.toString, l.mkString("\"", ",", "\"")) }
  
  store("metrics/lr-surface.csv", "w,b,l", metricsData.toList)