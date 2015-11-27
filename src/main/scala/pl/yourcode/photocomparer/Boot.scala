package pl.yourcode.photocomparer

object Boot extends Logging {
  def main(args: Array[String]): Unit = {
    val p1 = "/Users/kowolm/Desktop/Zdjecia i Filmy/Slub Nowaka"
    val p2 = "/Users/kowolm/Desktop/Zdjecia i Filmy/sss"
    val duplicates = FileDeduplicator(p1).duplicates(p2)
    duplicates.foreach(println)
    FileMover.move("/Users/kowolm/test", duplicates)
  }
}
