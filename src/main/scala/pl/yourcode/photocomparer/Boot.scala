package pl.yourcode.photocomparer

object SaveToFile {
  import java.io.{File, PrintWriter}

  def printToFile(fileName: String)(block: PrintWriter => Unit): Unit = {
    printToFile(new File(fileName))(block)
  }

  def printToFile(file: File)(block: PrintWriter => Unit): Unit = {
    val writer = new PrintWriter(file, "utf-8")
    try { block(writer) } finally { writer.close() }
  }
}

object Boot extends Logging {
  def main(args: Array[String]): Unit = {
    val p1 = """C:\Users\michal\Desktop\Nowy folder (2)\b"""
    val p2 = """C:\Users\michal\Desktop\Nowy folder (2)\a"""
    val duplicates = FileDeduplicator(p1).duplicates(p2)
    duplicates.foreach(println)

    val duplicatesInDir = FileDeduplicator(p1).duplicates()
    SaveToFile.printToFile("duplicates.txt") { out =>
      duplicatesInDir.foreach { files =>
        val filesText = files.map(_.getCanonicalPath).mkString("\n")
        out.println(filesText)
        out.println()
      }
    }
    FileMover.move("""C:\Users\michal\Desktop\Nowy folder (2)\c""", duplicates)
  }
}
