package pl.yourcode.photocomparer

import pl.yourcode.photocomparer.cleaner.CleanerCLI

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
    val mainPath = """C:\Users\michal\Desktop\Nowy folder (2)\b"""
    val secondaryPath = """C:\Users\michal\Desktop\Nowy folder (2)\a"""
    val moveToPath = """C:\Users\michal\Desktop\Nowy folder (2)\c"""

    new CleanerCLI(FileDeduplicator("""C:\Users\michal\Desktop\file-comparer""").duplicates()).run()
  }

  def deduplicate(mainPath: String, secondaryPath: String, moveToPath: String): Unit = {
    val duplicates = FileDeduplicator(mainPath).duplicates(secondaryPath)
    FileMover.move("""C:\Users\michal\Desktop\Nowy folder (2)\c""", duplicates)
  }

  private def duplicatesInDir(path: String): Unit = {
    val duplicatesInDir = FileDeduplicator(path).duplicates()
    SaveToFile.printToFile("duplicates.txt") { out =>
      duplicatesInDir.foreach { files =>
        val filesText = files.map(_.getCanonicalPath).mkString("\n")
        out.println(filesText)
        out.println()
      }
    }
  }
}
