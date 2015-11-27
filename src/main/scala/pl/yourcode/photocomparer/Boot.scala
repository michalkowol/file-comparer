package pl.yourcode.photocomparer

object Boot extends Logging {
  def main(args: Array[String]): Unit = {
    val p1 = """C:\Users\michal\Desktop\Nowy folder (2)\b"""
    val p2 = """C:\Users\michal\Desktop\Nowy folder (2)\a"""
    val duplicates = FileDeduplicator(p1).duplicates(p2)
    duplicates.foreach(println)
    FileMover.move("""C:\Users\michal\Desktop\Nowy folder (2)\c""", duplicates)
  }
}
