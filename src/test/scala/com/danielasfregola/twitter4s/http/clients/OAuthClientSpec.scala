package com.danielasfregola.twitter4s.http.clients


import com.danielasfregola.twitter4s.util.{ClientSpec, ClientSpecContext}

import scala.concurrent.Future
import scala.util.Failure

import spray.http.{HttpResponse, StatusCodes}
import com.danielasfregola.twitter4s.exceptions.{Errors, TwitterError, TwitterException}

class OAuthClientSpec extends ClientSpec {

  class OAuthClientSpecContext extends ClientSpecContext with OldOAuthClient {

    def exampleRequest(): Future[Unit] = Get("an-example-request").respondAs[Unit]
  }

  "OAuth OldClient" should {

    "throw twitter exception to twitter rejection" in new OAuthClientSpecContext {
      val response = {
        val entity = """{"errors":[{"message":"Sorry, that page does not exist","code":34}]}"""
        HttpResponse(StatusCodes.NotFound, entity)
      }
      when(exampleRequest)
        .expectRequest(identity(_))
        .respondWith(response)
        .onComplete {
          case Failure(e: TwitterException) =>
            e.code === StatusCodes.NotFound
            e.errors === Errors( TwitterError("Sorry, that page does not exist", 34) )
          case _ => failure("An Twitter Exception should be thrown")
        }
    }

    "throw twitter exception to generic failure http response" in new OAuthClientSpecContext {
      val response = HttpResponse(StatusCodes.RequestTimeout, "Something bad happened")
      when(exampleRequest)
        .expectRequest(identity(_))
        .respondWith(response)
        .onComplete {
          case Failure(e: TwitterException) =>
            e.code === StatusCodes.RequestTimeout
            e.errors === Errors()
          case _ => failure("An Twitter Exception should be thrown")
        }
    }

  }

}
