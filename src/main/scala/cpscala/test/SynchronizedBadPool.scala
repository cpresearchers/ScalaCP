package cpscala.test

import scala.collection._
import scala.collection.mutable.ArrayBuffer

object SynchronizedBadPool extends App {
  private val tasks = mutable.Queue[() => Unit]()

  val worker = new Thread {
    def poll(): Option[() => Unit] = tasks.synchronized {
      if (tasks.nonEmpty) Some(tasks.dequeue()) else None
    }

    override def run(): Unit = while (true) poll() match {
      case Some(tasks) => tasks
      case None =>
    }
  }

  worker.setName("Worker")
  worker.setDaemon(true)
  worker.start()
}
