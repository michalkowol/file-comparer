package pl.yourcode.photocomparer.hashing

import java.io.{FileInputStream, File, InputStream}

trait FileHasher {
  def hash(file: File): Hash = {
    val inputStream = new FileInputStream(file)
    val hashValue = hash(inputStream)
    inputStream.close()
    hashValue
  }

  def hash(in: InputStream): Hash
}
