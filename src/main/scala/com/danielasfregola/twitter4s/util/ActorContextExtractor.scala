package com.danielasfregola.twitter4s.util

import scala.concurrent.ExecutionContext
import akka.event.LoggingAdapter

import spray.util.LoggingContext
import com.danielasfregola.twitter4s.providers.{ExecutionContextProvider, ActorSystemProvider}

trait ActorContextExtractor extends ExecutionContextProvider with ActorSystemProvider {

  implicit val log: LoggingAdapter = LoggingContext.fromActorRefFactory(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

}
