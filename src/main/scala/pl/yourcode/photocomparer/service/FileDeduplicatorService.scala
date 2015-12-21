package pl.yourcode.photocomparer.service

import java.io.File

import pl.yourcode.photocomparer.FileDeduplicator

import scala.concurrent.{ExecutionContext, Future}

class FileDeduplicatorService(implicit ec: ExecutionContext) {
  def findDuplicates(directory: String, progressListener: ProgressListener): Future[Seq[Seq[File]]] = Future {
    FileDeduplicator(directory, progressListener).duplicates()
  }
}
