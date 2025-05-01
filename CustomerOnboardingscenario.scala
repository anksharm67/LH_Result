package scenario

import io.gatling.core.Predef.scenario
import io.gatling.core.Predef._
import varibales.Variables._
import APIScripts._


object CustomerOnboardingscenario {

  val SC04_CustomerOnboarding = scenario("SC04_CustomerOnboarding")
      .feed(cust_onb_user)
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


    .forever
      {
      exec(initSession)
        .pause(3)


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


        .exec(CustomerOnboarding.T03_GetUser.getuser)
      .pause(3)
      .exec(CustomerOnboarding.T04_GetOutletById.getOutletById)
        .pause(3)
      .exec(CustomerOnboarding.T05_GetDistributors.getDistributors)
      .pause(3)
//      .exec(CustomerOnboarding.T06_GetOutletByCustomerid.GetOutletByCustomerid)
//      .pause(3)
      .exec(CustomerOnboarding.T07_GetOutletByUserId.getOutletByUserId)
      .pause(3)
      .exec(CustomerOnboarding.T08_VerifyUser.verifyUser)
      .pause(3)
//      .exec(CustomerOnboarding.T09_SetUserContext.setUserContext)
//      .pause(3)
      .exec(CustomerOnboarding.T10_UpdatePreferences.UpdatePreferences)
      .pause(3)
//      .exec(CustomerOnboarding.T11_UpdateSupplier.UpdateSupplier)
  }
}