package pl.yourcode.photocomparer

import java.io.File

import pl.yourcode.photocomparer.hashing.{MultiFileHasher, FileHashers, DirectoryHashers}
import pl.yourcode.photocomparer.service.{NoOpProgressListener, ProgressListener}

object FileDeduplicator {
  def apply(mainPath: String): FileDeduplicator = {
    val mainDir = new File(mainPath)
    apply(mainDir, NoOpProgressListener)
  }

  def apply(mainPath: String, progressListener: ProgressListener): FileDeduplicator = {
    val mainDir = new File(mainPath)
    apply(mainDir, progressListener)
  }

  def apply(main: Directory, progressListener: ProgressListener): FileDeduplicator = {
    val fileHasher = FileHashers.md5Hasher
    val multiFileHasher = new MultiFileHasher(fileHasher, progressListener)
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
