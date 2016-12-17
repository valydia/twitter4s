package com.danielasfregola.twitter4s.util

import akka.actor.ActorRef
import akka.testkit.TestProbe
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import com.danielasfregola.twitter4s.http.clients.{OldOAuthClient, StreamingOAuthClient}
import org.specs2.matcher.NoConcurrentExecutionContext
import org.specs2.specification.Scope
import spray.client.pipelining._
import spray.http._

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}

abstract class ClientSpecContext extends TestActorSystem with StreamingOAuthClient with OldOAuthClient with FixturesSupport with AwaitableFuture with NoConcurrentExecutionContext with Scope {
  self =>

  val consumerToken = ConsumerToken("consumer-key", "consumer-secret")
  val accessToken = AccessToken("access-key", "access-secret")

  private val transport = TestProbe()

  implicit val timeout: FiniteDuration = 20 seconds

  override def sendReceive: spray.client.pipelining.SendReceive = {
    implicit val timeout: Timeout = self.timeout
    spray.client.pipelining.sendReceive(transport.ref)
  }

  override def sendReceiveStream(requester: ActorRef): SendReceive = sendReceive

  def when[T](future: Future[T]): RequestMatcher[T] = new RequestMatcher(future)

  class RequestMatcher[T](future: Future[T]) {
    protected def responder = new Responder(future)

    def expectRequest(req: HttpRequest): Responder[T] = {
      transport.expectMsg(req)
      responder
    }

    def expectRequest(fn: HttpRequest => Unit) = {
      transport.expectMsgPF() {
        case req: HttpRequest => fn(req)
      }
      responder
    }
  }

  class Responder[T](future: Future[T]) {
    def respondWith(res: HttpResponse): Future[T] = { transport.reply(res); future }

    def respondWith(resourcePath: String): Future[T] =
      respondWith(HttpResponse(StatusCodes.OK, HttpEntity(MediaTypes.`application/json`, loadJson(resourcePath))))

    def respondWithOk: Future[Unit] = {
      val response = HttpResponse(StatusCodes.OK, HttpEntity(MediaTypes.`application/json`, """{"code": "OK"}"""))
      transport.reply(response)
      Future(())
    }
  }

  implicit class RichUri(val uri: Uri) {

    def endpoint = s"${uri.scheme}:${uri.authority}${uri.path}"
  }

}
