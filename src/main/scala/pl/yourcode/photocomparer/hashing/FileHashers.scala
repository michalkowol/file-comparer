package pl.yourcode.photocomparer.hashing

import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.CRC32

object FileHashers {
  def crcHasher: FileHasher = new FileCRC
  def md5Hasher: FileHasher = new FileMD5

  private trait FileReader {
    def read(in: InputStream)(applyBuffer: (Array[Byte], Int, Int) => Unit): Unit = {
      val buffer = new Array[Byte](1024)
      var numRead = 0
      do {
        numRead = in.read(buffer)
        if (numRead > 0) {
          applyBuffer(buffer, 0, numRead)
        }
      } while (numRead != -1)
    }
  }

  private class FileCRC extends FileHasher with FileReader {
    def hash(in: InputStream): Hash = {
      val crc32 = new CRC32
      read(in)((buffer, offset, len) => crc32.update(buffer, offset, len))
      crc32.getValue.toString
    }
  }

  private class FileMD5 extends FileHasher with FileReader {
    private def checksum(in: InputStream): Array[Byte] = {
      val md5 = MessageDigest.getInstance("MD5")
      read(in)((buffer, offset, len) => md5.update(buffer, offset, len))
      md5.digest()
    }

    def hash(in: InputStream): Hash = {
      val checksumValue = checksum(in)
      val md5 = checksumValue.map(e => Integer.toString((e & 0xff) + 0x100, 16).substring(1)).mkString("")
      md5
    }
  }
}
