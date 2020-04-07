import zio.test._
import zio.test.Assertion._

object MainSpec extends DefaultRunnableSpec {
  def spec = suite("main")(
    testM("basic config") {
      assertM(Main.parseArgs(List("foo", "bar")))(hasField("file", _.jar.getName, equalTo("foo")))
    }
  )
}
