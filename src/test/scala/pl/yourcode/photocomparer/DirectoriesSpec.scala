package pl.yourcode.photocomparer

import java.io.File
import org.scalatest.{FlatSpec, Matchers}

class DirectoriesSpec extends FlatSpec with Matchers {
  "Directories" should "list all files in directory and subdirectories" in {
    // when
    val filesInDirs = Directories.files(new File("."))

    // then
    filesInDirs.size should be >= 10
  }

  it should "throw an exception if you are trying start point is not directory" in {
    an[IllegalArgumentException] should be thrownBy {
      Directories.files(new File("build.sbt"))
    }
  }

  it should "throw an exception if directory does not exist" in {
    an[IllegalArgumentException] should be thrownBy {
      Directories.files(new File("not_existing_dir"))
    }
  }
}
