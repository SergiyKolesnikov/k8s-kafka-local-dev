object Main extends App {
  while (true) {
    val source = scala.io.Source.fromFile("/data/message.txt")
    val lines = try source.mkString finally source.close()
    print(lines)
    Thread.sleep(3000)
  }
}
