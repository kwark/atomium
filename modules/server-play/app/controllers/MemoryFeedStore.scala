package controllers

import be.vlaanderen.awv.atom.{UrlBuilder, Context, FeedStore}
import be.vlaanderen.awv.atom.format._
import org.joda.time.LocalDateTime

import scala.collection.mutable.ListBuffer

class MemoryFeedStore[T](feedName: String, baseUrl: Url, title : Option[String]) extends FeedStore[T] {
  val entries: ListBuffer[(List[T], LocalDateTime)] = new ListBuffer[(List[T], LocalDateTime)]

  val urlProvider : UrlBuilder = new UrlBuilder {

    override def base: Url = baseUrl

    override def feedLink(start: Int, count: Int): Url = Url(feedName) / start.toString / count.toString

    override def collectionLink: Url = ???
  }

  override def context: Context = ???

  override def getFeed(start: Int, pageSize: Int): Option[Feed[T]] = {
    entries.drop(start).take(pageSize).reverse.toList match {
      case Nil => None
      case l => Some(Feed[T](
        base = baseUrl,
        id = (baseUrl / feedName).path,
        title = title,
        updated = l.head._2.toDateTime(),
        links = List(link(Link.selfLink, start, pageSize),
          link(Link.lastLink, 0, pageSize)) ++
          getNextLink(start, pageSize) ++
          getPreviousLink(start, pageSize, entries.size),
        entries = l map { e => Entry(Content(e._1, contentType), Nil) }
      ))
    }
  }

  override def getHeadOfFeed(pageSize: Int): Option[Feed[T]] = {
    getHeadOfFeed(pageSize, entries.size)
  }

  override def push(it: Iterable[T]) = {
    val dateTime = new LocalDateTime()
    it foreach { t =>
      entries append ((t, dateTime))
    }
  }

}
