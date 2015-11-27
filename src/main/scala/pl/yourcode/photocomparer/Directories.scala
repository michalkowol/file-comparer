package pl.yourcode.photocomparer

import java.io.File
import com.google.common.io.Files

object Directories {
  def files(root: Directory): Seq[File] = {
    require(root.exists, "File does not exist")
    require(root.isDirectory, "Not a directory")
    allRegularFilesInDirectoryAndSubdirectories(root)
  }

  private def allRegularFilesInDirectoryAndSubdirectories(root: Directory): Seq[File] = {
    import scala.collection.JavaConversions.asScalaBuffer
    val filesAndDirs = Files.fileTreeTraverser().breadthFirstTraversal(root).toList.toList
    val files = filesAndDirs.filter { file => file.isFile }
    files
  }
}
