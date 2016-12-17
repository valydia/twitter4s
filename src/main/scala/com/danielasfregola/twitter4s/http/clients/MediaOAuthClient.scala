package com.danielasfregola.twitter4s.http.clients

private[twitter4s] trait MediaOAuthClient extends OAuthClient {

  override val withLogRequest: Boolean = true
  override val withLogRequestResponse: Boolean = false

}


