package pl.yourcode.photocomparer

import java.io.File

import com.google.common.io.Files

object FileMover {
  def move(moveToDirectoryName: String, files: Seq[File]): Unit = {
    files.foreach { file =>
      val moveTo = new File(moveToDirectoryName + file.getCanonicalPath)
      Files.createParentDirs(moveTo)
      Files.move(file, moveTo)
    }
  }
}
