package pl.yourcode.photocomparer

import java.io.File

import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.{Matchers, FlatSpec}
import pl.yourcode.photocomparer.hashing.DirectoryHashers

class FileDeduplicatorSpec extends FlatSpec with Matchers with MockitoSugar {

  "FileDeduplicator" should "find all duplicates in folder" in {
    // given
    val fileA = mock[File]
    val fileB = mock[File]
    val directoryHashers = mock[DirectoryHashers]
    when(directoryHashers.hash(any())).thenReturn(
      Map("1" -> Seq(fileA), "2" -> Seq(fileA, fileA), "3" -> Seq(fileA, fileA, fileA), "4" -> Seq(), "5" -> Seq(fileB, fileB))
    )
    val fileDeduplicator = new FileDeduplicator(fileA, directoryHashers)

    // when
    val duplicates = fileDeduplicator.duplicates()

    // then
    duplicates should have size 3
    duplicates should contain (Seq(fileA, fileA))
    duplicates should contain (Seq(fileA, fileA, fileA))
    duplicates should contain (Seq(fileB, fileB))
  }

  it should "find duplicates in two folders" in {
    // given
    val fileA = mock[File]
    val fileB = mock[File]
    val directoryHashers = mock[DirectoryHashers]
    when(directoryHashers.hash(fileA)).thenReturn(
      Map("1" -> Seq(fileA), "2" -> Seq(fileA, fileA), "3" -> Seq(fileA, fileA, fileA), "4" -> Seq(), "5" -> Seq(fileB, fileB))
    )
    when(directoryHashers.hash(fileB)).thenReturn(
      Map("2" -> Seq(fileA, fileA), "5" -> Seq(fileB, fileB), "6" -> Seq(fileB, fileB), "7" -> Seq(fileB, fileB), "3" -> Seq(fileA, fileA, fileA))
    )
    val fileDeduplicator = new FileDeduplicator(fileA, directoryHashers)

    // when
    val duplicates = fileDeduplicator.duplicates(fileB)

    // then
    duplicates should have size 7
  }
}
