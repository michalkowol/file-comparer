package pl.yourcode.photocomparer.hashing

import java.io.File

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.mock.MockitoSugar

class MultiFileHasherSpec extends FlatSpec with Matchers with MockitoSugar {

  "A File Hasher" should "hash many files" in {
    // given
    def manyFakeFiles(fakeFileCount: Int): Seq[File] = (0 until fakeFileCount).map(_ => mock[File])
    val hasher = mock[FileHasher]
    when(hasher.hash(any[File])).thenReturn("1", "2", "2", "1", "1", "1", "3")

    // when
    val filesWithHashes = new MultiFileHasher(hasher).hash(manyFakeFiles(10))

    // then
    filesWithHashes should have size 3
    filesWithHashes.get("0") shouldBe Option.empty
    filesWithHashes("1") should have size 4
    filesWithHashes("2") should have size 2
    filesWithHashes("3") should have size 4
  }
}
