package pl.yourcode.photocomparer.hashing

import java.io.File

import pl.yourcode.photocomparer.Logging
import pl.yourcode.photocomparer.service.{NoOpProgressListener, ProgressListener}

import scala.annotation.tailrec
import scala.collection.mutable.{Map => MMap}

class MultiFileHasher(fileHasher: FileHasher, progressListener: ProgressListener = NoOpProgressListener) extends Logging {

  def hash(files: Seq[File]): Map[Hash, Seq[File]] = {
    @tailrec
    def go(remainingFiles: Seq[File], acc: MMap[Hash, Seq[File]], counter: Int): Map[Hash, Seq[File]] = remainingFiles match {
      case file +: rest =>
        if (counter % 100 == 0) progressListener.update(counter * 100 / files.size)
        val hashValue = fileHasher.hash(file)
        val filesWithSameHash = acc.getOrElse(hashValue, Seq.empty)
        acc(hashValue) = file +: filesWithSameHash
        go(rest, acc, counter + 1)
      case _ => acc.toMap
    }
    go(files, MMap.empty, 0)
  }
}
