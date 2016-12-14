package com.danielasfregola.twitter4s.http.clients

import com.danielasfregola.twitter4s.exceptions.{Errors, TwitterException}
import com.danielasfregola.twitter4s.http.unmarshalling.{JsonSupport, OldJsonSupport}
import com.danielasfregola.twitter4s.providers.ActorSystemProvider
import com.danielasfregola.twitter4s.util.ActorContextExtractor
import org.json4s.native.Serialization

import scala.concurrent.Future
import scala.util.Try
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContext, Future}

trait Client extends JsonSupport with ActorContextExtractor { self: ActorSystemProvider =>

  def host: String

  val withLogRequest = false
  val withLogRequestResponse = true

  private[twitter4s] implicit class RichHttpRequest(val request: HttpRequest) {
    def respondAs[T: Manifest]: Future[T] = sendReceiveAs[T](request)
  }

  val httpClient = Http().outgoingConnection(host = host)

  def sendReceiveAs[T: Manifest](httpRequest: HttpRequest): Future[T] =
    sendAndReceive(httpRequest, response => json4sUnmarshaller[T].apply(response.entity))

  private def sendAndReceive[T](request: HttpRequest, f: HttpResponse => Future[T]): Future[T] = {
    implicit val rqst = request
    val requestStartTime =  System.currentTimeMillis
    if (withLogRequest) logRequest(request)
    Source
    .single(request)
    .via(httpClient)
    .mapAsync(1) { implicit response => unmarshal(requestStartTime, f) }
    .runWith(Sink.head)
  }

  private def unmarshal[T](requestStartTime: Long, f: HttpResponse => Future[T])(implicit request: HttpRequest, response: HttpResponse) = {
    if (withLogRequestResponse) logRequestResponse(requestStartTime)

    if (response.status.isSuccess) f(response)
    else {
      response.entity.toStrict(50 seconds).map { sink =>
        val body = sink.data.utf8String
        val errors = Try {
          Serialization.read[Errors](body)
        } getOrElse Errors()
        throw new TwitterException(response.status, errors)
      }
    }
  }

  // TODO - logRequest, logRequestResponse customisable?
  def logRequest: HttpRequest => HttpRequest = { request =>
    log.info("{} {}", request.method, request.uri)
    log.debug("{} {} | {} | {}", request.method, request.uri, request.entity, request)
    request
  }

  def logRequestResponse(requestStartTime: Long)(implicit request: HttpRequest): HttpResponse => HttpResponse = { response =>
    val elapsed = System.currentTimeMillis - requestStartTime
    log.info("{} {} ({}) | {}ms", request.method, request.uri, response.status, elapsed)
    log.debug("{} {} ({}) | {}", request.method, request.uri, response.status, response.entity)
    response
  }

//  // TODO - logRequest, logResponse customisable?
//  def pipeline[T: FromResponseUnmarshaller]: HttpRequest => Future[T]
//
//  private[twitter4s] def sendReceiveAs[T: FromResponseUnmarshaller](request: HttpRequest) =
//    pipeline apply request
//
//  private[twitter4s] def sendReceive = spray.client.pipelining.sendReceive
//
//  def logRequest: HttpRequest => HttpRequest = { request =>
//    log.info("{} {}", request.method, request.uri)
//    log.debug("{} {} | {} | {}", request.method, request.uri, request.entity.asString, request)
//    request
//  }
//
//  def logResponse(requestStartTime: Long)(implicit request: HttpRequest): HttpResponse => HttpResponse = { response =>
//    val elapsed = System.currentTimeMillis - requestStartTime
//    log.info("{} {} ({}) | {}ms", request.method, request.uri, response.status, elapsed)
//    log.debug("{} {} ({}) | {}", request.method, request.uri, response.status, response.entity.asString)
//    response
//  }
//
//  private[twitter4s] def unmarshalResponse[T: FromResponseUnmarshaller]: HttpResponse ⇒ T = { hr =>
//    hr.status.isSuccess match {
//      case true => hr ~> unmarshal[T]
//      case false =>
//        val errors = Try {
//          Serialization.read[Errors](hr.entity.asString)
//        } getOrElse { Errors() }
//        throw new TwitterException(hr.status, errors)
//    }
//  }
//
//  private[twitter4s] def unmarshalEmptyResponse: HttpResponse ⇒ Unit = { hr =>
//    if (hr.status.isFailure) {
//      val errors = Try {
//        Serialization.read[Errors](hr.entity.asString)
//      } getOrElse { Errors() }
//      throw new TwitterException(hr.status, errors)
//    }
//  }

}
