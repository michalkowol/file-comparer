package pl.yourcode.photocomparer

import java.io.File

import com.google.common.io.Files

object FileOperations {
  def move(moveToDirectoryName: String, files: Seq[File]): Unit = {
    files.foreach { file =>
      val moveTo = new File(moveToDirectoryName + file.getCanonicalPath.replaceAll("""^.:\\""", """\\"""))
      Files.createParentDirs(moveTo)
      Files.move(file, moveTo)
    }
  }

  def deleteAll(files: Seq[File]): Unit = {
    files.foreach { file =>
      println(s"Deleted ${file.getCanonicalPath}...")
      //file.delete()
    }
  }
}
