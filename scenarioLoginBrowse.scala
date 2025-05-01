package scenario

import APIScripts._
import io.gatling.core.Predef._
import varibales.Variables._

object scenarioLoginBrowse {

  val SC01_login_browse = scenario("SC01_CB_Laos_Browse_Logged")
    .feed(env_details)
//      .exec(Scripts.LoginAccessToken.loginaccesstoken)
//      .pause(3)

.feed(login_credentials)
    .feed(env_details)
    .exec(JWTtokenGeneration.LoginPage.LoginPage)
    .pause(5)
    .exec(JWTtokenGeneration.JWTtokenGenration.JWTtokenGenration)
    .pause(5)
    .exec(JWTtokenGeneration.AccessTokenGeneration.AccessTokenGeneration)
    .pause(5)


    .exec(session => {
      // Store the access token timestamp in seconds
      session.set("lastTokenTime", System.currentTimeMillis() / 1000)
    })

    .forever {
      exec(initSession)

        .exec(session => {
          val currentTimeSec = System.currentTimeMillis() / 1000
          val lastTokenTimeSec = session("lastTokenTime").as[Long]
          val tokenLifetime = currentTimeSec - lastTokenTimeSec

          //          println("currentTimeSec = ", currentTimeSec)
          //          println("lastTokenTimeSec = ", lastTokenTimeSec)
          //          println("tokenLifetime = ", tokenLifetime)

          // If 3600 seconds (1 hour) passed, mark for token refresh
          if (currentTimeSec - lastTokenTimeSec >= 3500) {
            println("Access token expired — regenerating...")
            session.set("refreshToken", true)
          } else {
            session.set("refreshToken", false)
          }
        })

        .doIf(session => session("refreshToken").as[Boolean]) {

          exec(JWTtokenGeneration.LoginPage.LoginPage)
            .pause(5)
            .exec(JWTtokenGeneration.JWTtokenGenration.JWTtokenGenration)
            .pause(5)
            .exec(JWTtokenGeneration.AccessTokenGeneration.AccessTokenGeneration)
            .pause(5)

            .exec(session => session.set("lastTokenTime", System.currentTimeMillis() / 1000))
        }

//        .exec(Scripts.PDP.pdp)
//        .pause(3)
//        .exec(Scripts.AlternateProducts.alternateProducts)
        .exec(APIScripts.BrowseLatest.categories_latest.categories)
        .pause(3)
        .exec(APIScripts.BrowseLatest.PLP_Latest.plppage)
        .pause(3)
        .exec(APIScripts.BrowseLatest.PDP.pdp)
        .pause(3)
        .exec(APIScripts.BrowseLatest.AlternateProducts.alternateProducts)
        .pause(3)
    }



}
