package com.danielasfregola.twitter4s.http.clients.rest.trends

import com.danielasfregola.twitter4s.util.{ClientSpec, ClientSpecContext}
import spray.http.HttpMethods
import spray.http.Uri.Query
import com.danielasfregola.twitter4s.entities.{LocationTrends, Location}

class TwitterTrendClientSpec  extends ClientSpec {

  class TwitterTrendClientSpecContext extends ClientSpecContext with TwitterTrendClient

  "Twitter Trend OldClient" should {

    "get global trends" in new TwitterTrendClientSpecContext {
      val result: LocationTrends = when(getGlobalTrends()).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/trends/place.json"
        request.uri.query === Query("id=1")
      }.respondWith("/twitter/rest/trends/trends.json").await
      result === loadJsonAs[LocationTrends]("/fixtures/rest/trends/trends.json")
    }

    "get trends for a location" in new TwitterTrendClientSpecContext {
      val result: LocationTrends = when(getTrends(1)).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/trends/place.json"
        request.uri.query === Query("id=1")
      }.respondWith("/twitter/rest/trends/trends.json").await
      result === loadJsonAs[LocationTrends]("/fixtures/rest/trends/trends.json")
    }

    "get trends for a location without hashtags" in new TwitterTrendClientSpecContext {
      val result: LocationTrends = when(getTrends(1, true)).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/trends/place.json"
        request.uri.query === Query("exclude=hashtags&id=1")
      }.respondWith("/twitter/rest/trends/trends.json").await
      result === loadJsonAs[LocationTrends]("/fixtures/rest/trends/trends.json")
    }

    "get locations with available trends" in new TwitterTrendClientSpecContext {
      val result: Seq[Location] = when(getLocationTrends).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/trends/available.json"
      }.respondWith("/twitter/rest/trends/available_locations.json").await
      result === loadJsonAs[Seq[Location]]("/fixtures/rest/trends/available_locations.json")
    }

    "get closest location trends" in new TwitterTrendClientSpecContext {
      val result: Seq[Location] = when(getClosestLocationTrends(37.781157, -122.400612831116)).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/trends/closest.json"
      }.respondWith("/twitter/rest/trends/closest_locations.json").await
      result === loadJsonAs[Seq[Location]]("/fixtures/rest/trends/closest_locations.json")
    }
    
  }

}
