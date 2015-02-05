package be.wegenenverkeer.atomium.client

import be.wegenenverkeer.atomium.client.FeedEntryIterator.Implicits._
import be.wegenenverkeer.atomium.format.{Feed, Link, Url}
import be.wegenenverkeer.atomium.server.{Context, FeedService, MemoryFeedStore}
import org.joda.time.DateTime

import scala.util.{Success, Try}

/**
 * A trait fixture to be used on tests requiring a [[be.wegenenverkeer.atomium.client.FeedEntryIterator]] or eventually
 * an [[be.wegenenverkeer.atomium.client.async.AsyncFeedEntryIterator]].
 *
 * It's backed by a blocking in-memory [[MemoryFeedStore]] and [[FeedService]].
 *
 * Entries must be pushed prior to the initialization of the `Iterator`.
 *
 * {{{
 *
 *  import scala.concurrent.duration._
 *
 *  new FeedIteratorFixture[String] {
 *
 *    push("a1", "b1", "c1", "a2", "b2", "c2")
 *
 *    // start an iterator from the least recent entry
 *    // will iterate over all entries starting at 'a1' and ending at 'c2'
 *    val iter1 = iteratorFromStart
 *
 *    // start an iterator from 'c1'
 *    // will iterate over 'a2', 'b2' and 'c2'
 *    val iter2 = iteratorStartingFrom("c1")
 *  }
 *
 * }}}
 *
 *
 * Entry ids are automatically generated by the `FeedStore`. In case you need to retrieve the id
 * for a given entry. You can find it by entry content value.
 *
 * {{{
 *
 *  import scala.concurrent.duration._
 *
 *  new FeedIteratorFixture[String] {
 *
 *    push("a1", "b1", "c1", "a2", "b2", "c2")
 *
 *    val entryRef = iteratorFromStart.findByValue("c1")
 *    val id = entryRef.entryId
 *  }
 * }}}
 *
 *
 * Note: although the backing `FeedStore`,  `FeedService` and `FeedProvider` are blocking, the underlying
 * iterator is not. Therefore, you can request a async iterator in case your tests requires one.
 *
 * *
 * {{{
 *
 *  import scala.concurrent.duration._
 *
 *  new FeedIteratorFixture[String] {
 *
 *    push("a1", "b1", "c1", "a2", "b2", "c2")
 *
 *    val asyncIter = iteratorFromStart.asyncIterator
 *  }
 * }}}
 *
 * @tparam E the type of the Feed content value.
 */
trait FeedIteratorFixture[E] {


  def pageSize: Int = 10

  def baseUrl: String = "http://feed-iterator-fixture/feeds"

  //dummy context for MemoryFeedStore

  private implicit val context: Context = new Context {}

  private lazy val feedStore = new MemoryFeedStore[E, Context]("test", Url(baseUrl), Some("test"), "text/plain")
  private lazy val feedService = new FeedService[E, Context]("test", pageSize, feedStore)

  def push(values: E*): Unit = push(values.toList)

  def push(values: List[E]): Unit = feedService.push(values)


  def iteratorFromStart: FeedEntryIterator[E] = {
    TestFeedProvider.iterator()
  }

  def iteratorStartingFrom(entryRef: Option[EntryRef[E]]): FeedEntryIterator[E] = {
    TestFeedProvider.iterator(entryRef)
  }

  def iteratorStartingFrom(entryValue: E): FeedEntryIterator[E] = {
    // force NoSuchElementException if there is no such entry
    val entry = findByValue(_ == entryValue).get
    iteratorStartingFrom(Some(entry))
  }

  def findByValue(f: (E) => Boolean): Option[EntryRef[E]] = {
    iteratorFromStart.find { entryRef =>
      f(entryRef.entry.get.content.value)
    }
  }

  private case object TestFeedProvider extends FeedProvider[E] {

    /**
     * Return first feed or a Failure
     */
    override def fetchFeed(initialEntryRef: Option[EntryRef[E]] = None): Try[Feed[E]] = {
      initialEntryRef match {
        case None           => optToTry(fetchLastFeed)
        case Some(position) => fetchFeed(position.url.path)
      }

    }

    /**
     * Return feed whose selfLink equals 'page or Failure
     */
    override def fetchFeed(page: String): Try[Feed[E]] = {
      optToTry(getFeedPage(Url(page)))
    }

    private def fetchLastFeed: Option[Feed[E]] = {
      val lastFeed =
        for {
          feed <- feedService.getHeadOfFeed()
          lastUrl <- feed.lastLink
          lastFeed <- getFeedPage(lastUrl.href)
        } yield lastFeed
      lastFeed
    }

    private def getFeedPage(pageUrl: Url): Option[Feed[E]] = {
      val params = pageUrl.path.replaceFirst(baseUrl + "/", "").split("/")
      val page = params(0).toInt
      val isForward = params(1) == "forward"
      val pageSize = params(2).toInt
      feedService.getFeedPage(page, pageSize, forward = isForward)
    }


    private def optToTry(feedOpt: Option[Feed[E]]): Success[Feed[E]] = {

      def emptyFeed = {
        val links = List(Link(Link.selfLink, Url(baseUrl)))
        Feed(
          id = "N/A",
          base = Url(baseUrl),
          title = Option("title"),
          generator = None,
          updated = new DateTime(),
          links = links,
          entries = List()
        )
      }
      feedOpt.map { feed =>
        Success(feed)
      }.getOrElse(Success(emptyFeed))
    }

  }

}
