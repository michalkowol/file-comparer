package pl.yourcode.photocomparer.cleaner

// scalastyle:off

import java.awt.Desktop
import java.io.File
import java.net.URI

import com.paypal.cascade.common.option._

object CleanerCLI {
  def apply(filesNames: Seq[Seq[String]]): CleanerCLI = {
    val files = filesNames.map { filesNames =>
      filesNames.map(file => new File(file))
    }
    new CleanerCLI(files)
  }
}

class CleanerCLI(files: Seq[Seq[File]]) {

  def run(): Unit = {
    val desktop = if (Desktop.isDesktopSupported) Desktop.getDesktop.some else none
    desktop.filter(desktop => desktop.isSupported(Desktop.Action.BROWSE)).map(desktop => desktop.browse(URI.create("http://localhost:8080")))
    choose()
  }

  private def choose(): Unit = {
    def go(files: Seq[Seq[File]], history: Seq[Seq[File]], toLeaveFiles: Seq[File]): Seq[File] = files match {
      case filesHead +: filesTail =>
        val cout = filesHead.zipWithIndex.map { case (file, index) => s"[${index + 1}] ${file.getCanonicalPath}" }.mkString("\t\t\t")
        println(cout)
        val char = Console.in.read
        if (char == 8) {
          history match {
            case historyHead +: historyTail => go(historyHead +: files, historyTail, toLeaveFiles.tail)
            case Nil => go(files, history, toLeaveFiles)
          }
        } else {
          val toLeaveFile = filesHead match {
            case Seq(fileA, fileB) => if (char.toChar == 'a') fileA else fileB
            case manyFiles => manyFiles.head // 1,2,...,9,a,b,...,z
          }
          go(filesTail, filesHead +: history, toLeaveFile +: toLeaveFiles)
        }
      case Nil =>
        println("Finished?")
        val char = Console.in.read
        if (char == 8) {
          history match {
            case historyHead +: historyTail => go(historyHead +: files, historyTail, toLeaveFiles.tail)
            case Nil => toLeaveFiles
          }
        } else {
          toLeaveFiles
        }
    }
    val toLeave = go(files, history = Nil, toLeaveFiles = Nil)
    println(toLeave)
  }
}
