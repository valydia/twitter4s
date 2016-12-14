package com.danielasfregola.twitter4s.http.clients.rest.search

import com.danielasfregola.twitter4s.util.{ClientSpec, ClientSpecContext}
import spray.http.HttpMethods
import spray.http.Uri.Query
import com.danielasfregola.twitter4s.entities.StatusSearch

class TwitterSearchClientSpec extends ClientSpec {

  class TwitterSearchClientSpecContext extends ClientSpecContext with TwitterSearchClient

  "Twitter Search OldClient" should {

    "search for tweets" in new TwitterSearchClientSpecContext {
      val result: StatusSearch = when(searchTweet("#scala")).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/search/tweets.json"
        request.uri.query === Query("count=15&include_entities=true&q=%23scala&result_type=mixed")
      }.respondWith("/twitter/rest/search/tweets.json").await
      result === loadJsonAs[StatusSearch]("/fixtures/rest/search/tweets.json")
    }
  }

}
