package com.danielasfregola.twitter4s.exceptions

import akka.http.scaladsl.model.StatusCode

case class TwitterError(message: String, code: Int) {
  override def toString = s"$message ($code)"
}

case class Errors(errors: TwitterError*) {
  override def toString = errors.mkString(", ")
}

object Errors {

  def apply(throwable: Throwable): Errors = {
    val error = TwitterError(throwable.getMessage, code = -1)
    apply(error)
  }
}

case class TwitterException(code: StatusCode, errors: Errors) extends Exception(s"[$code] $errors")

object TwitterException {

  def apply(code: StatusCode, msg: String): TwitterException = {
    val error = TwitterError(msg, code.intValue)
    TwitterException(code, Errors(error))
  }
}
