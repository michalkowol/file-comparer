package pl.yourcode.photocomparer

import java.io.File

import pl.yourcode.photocomparer.hashing.{MultiFileHasher, FileHashers, DirectoryHashers}

object FileDeduplicator {
  def apply(mainPath: String): FileDeduplicator = {
    val mainDir = new File(mainPath)
    apply(mainDir)
  }

  def apply(main: Directory): FileDeduplicator = {
    val fileHasher = FileHashers.md5Hasher
    val multiFileHasher = new MultiFileHasher(fileHasher)
    val directoryHasher = new DirectoryHashers(multiFileHasher)
    new FileDeduplicator(main, directoryHasher)
  }
}

class FileDeduplicator(main: Directory, directoryHashers: DirectoryHashers) {
  def duplicates(): Seq[Seq[File]] = {
    val mainHashesAndFiles = directoryHashers.hash(main)
    val duplicates = mainHashesAndFiles.filterNot { case (_, files) => files.size <= 1 }
    duplicates.values.toSeq
  }

  def duplicates(otherPath: String): Seq[File] = {
    val other = new File(otherPath)
    duplicates(other)
  }

  def duplicates(other: Directory): Seq[File] = {
    val mainHashesAndFiles = directoryHashers.hash(main)
    val otherHashesAndFiles = directoryHashers.hash(other)

    val mainHashes = mainHashesAndFiles.keySet
    val otherHashes = otherHashesAndFiles.keySet

    val difference = otherHashes &~ mainHashes
    val duplicates = otherHashesAndFiles -- difference

    duplicates.values.flatten.toSeq
  }
}
