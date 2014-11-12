import be.vlaanderen.awv.atom._
import be.vlaanderen.awv.atom.format._
import controllers.{MemoryFeedStore, MyFeedController}
import play.api.GlobalSettings
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter()) with GlobalSettings {

  val id = "my_feed"
  val feedStore: FeedStore[String] = new MemoryFeedStore[String](id, Url("http://localhost:9000/feeds/"), Some("strings of life"))
  val feedService = new FeedService[String, Context](id, 2, { (s, c) => feedStore })
  val feedController = new MyFeedController(feedService)

  //add some dummy values to the feedservice
  implicit val c: Context = new Context() {}
  feedService.push("foo")
  feedService.push(List("bar", "baz"))
  feedService.push("foobar")

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    if (controllerClass == classOf[MyFeedController]) {
      feedController.asInstanceOf[A]
    } else {
      super.getControllerInstance(controllerClass)
    }
  }
}