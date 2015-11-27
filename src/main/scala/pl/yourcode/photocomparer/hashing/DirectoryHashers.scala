package pl.yourcode.photocomparer.hashing

import java.io.File

import pl.yourcode.photocomparer.{Directory, Directories}

class DirectoryHashers(multiFileHasher: MultiFileHasher) {
  def hash(root: Directory): Map[Hash, Seq[File]] = {
    require(root.exists, "File does not exist")
    require(root.isDirectory, "Not a directory")
    val allFiles = Directories.files(root)
    multiFileHasher.hash(allFiles)
  }
}
