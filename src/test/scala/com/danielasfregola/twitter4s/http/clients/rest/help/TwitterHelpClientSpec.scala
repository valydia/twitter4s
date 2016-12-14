package com.danielasfregola.twitter4s.http.clients.rest.help

import com.danielasfregola.twitter4s.util.{ClientSpec, ClientSpecContext}
import spray.http.HttpMethods
import com.danielasfregola.twitter4s.entities.{LanguageDetails, TermsOfService, PrivacyPolicy, Configuration}

class TwitterHelpClientSpec extends ClientSpec {

  class TwitterHelpClientSpecContext extends ClientSpecContext with TwitterHelpClient

  "Twitter Help OldClient" should {

    "get twitter configuration" in new TwitterHelpClientSpecContext {
      val result: Configuration = when(getConfiguration()).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/help/configuration.json"
      }.respondWith("/twitter/rest/help/configuration.json").await
      result === loadJsonAs[Configuration]("/fixtures/rest/help/configuration.json")
    }

    "get supported languages" in new TwitterHelpClientSpecContext {
      val result: Seq[LanguageDetails] = when(getSupportedLanguages()).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/help/languages.json"
      }.respondWith("/twitter/rest/help/languages.json").await
      result === loadJsonAs[Seq[LanguageDetails]]("/fixtures/rest/help/languages.json")
    }
    
    "get twitter privacy policy" in new TwitterHelpClientSpecContext {
      val result: PrivacyPolicy = when(getPrivacyPolicy()).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/help/privacy.json"
      }.respondWith("/twitter/rest/help/privacy.json").await
      result === loadJsonAs[PrivacyPolicy]("/fixtures/rest/help/privacy.json")
    }

    "get twitter terms of service" in new TwitterHelpClientSpecContext {
      val result: TermsOfService = when(getTermsOfService()).expectRequest { request =>
        request.method === HttpMethods.GET
        request.uri.endpoint === "https://api.twitter.com/1.1/help/tos.json"
      }.respondWith("/twitter/rest/help/tos.json").await
      result === loadJsonAs[TermsOfService]("/fixtures/rest/help/tos.json")
    }
  }
}
