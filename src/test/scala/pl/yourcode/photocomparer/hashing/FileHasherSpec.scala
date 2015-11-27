package pl.yourcode.photocomparer.hashing

import org.scalatest.{Matchers, FlatSpec}

class FileHasherSpec extends FlatSpec with Matchers {
  "A CRC32 hasher" should "hash files" in {
    checkHasher(FileHashers.crcHasher)
  }

  it should "give file specific hash" in {
    checkHash(FileHashers.crcHasher, "/fileA.txt", "1361531379")
    checkHash(FileHashers.crcHasher, "/fileB.txt", "1361531379")
    checkHash(FileHashers.crcHasher, "/fileC.txt", "1435631589")
  }

  "A MD5 hasher" should "hash files" in {
    checkHasher(FileHashers.md5Hasher)
  }

  it should "give file specific hash" in {
    checkHash(FileHashers.md5Hasher, "/fileA.txt", "6ed2be5fe9c42a45b3a4062e1b6c9229")
    checkHash(FileHashers.md5Hasher, "/fileB.txt", "6ed2be5fe9c42a45b3a4062e1b6c9229")
    checkHash(FileHashers.md5Hasher, "/fileC.txt", "45df1f7146dc403fb7b4a4b81818c1b4")
  }

  def checkHash(fileHasher: FileHasher, file: String, expectedHash: String): Unit = {
    // given
    val b1 = classOf[FileHasherSpec].getResourceAsStream(file)

    // when
    val hash = fileHasher.hash(b1)

    // then
    hash shouldEqual expectedHash
  }

  def checkHasher(fileHasher: FileHasher): Unit = {
    // given
    val b1 = classOf[FileHasherSpec].getResourceAsStream("/fileA.txt")
    val b2 = classOf[FileHasherSpec].getResourceAsStream("/fileB.txt")
    val b3 = classOf[FileHasherSpec].getResourceAsStream("/fileC.txt")

    // when
    val hash1 = fileHasher.hash(b1)
    val hash2 = fileHasher.hash(b2)
    val hash3 = fileHasher.hash(b3)

    // then
    hash1 shouldEqual hash2
    hash1 should not equal hash3
  }
}
