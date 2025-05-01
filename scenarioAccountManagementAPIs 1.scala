package scenario

import APIScripts._
import io.gatling.core.Predef._
import varibales.Variables._
object scenarioAccountManagementAPIs {

  val SC03_AccountManagement = scenario("SC03_AccountManagement")
    .feed(env_details)
      .feed(login_credentials)
//////////////////////////////////////    sai krishna told not to create token with this
//    .exec(AccountManagementScripts.LoginAccessTokenSIT.loginAccessTokenSIT)
//    .pause(3)
////////////////////////////////////////// Handling login in simulation
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

      .exec(AccountManagementScripts.T03_GetUser.getUser)
      .pause(3)
      .exec(AccountManagementScripts.T04_GetPreferences.getPreferences)
      .pause(3)
      .exec(AccountManagementScripts.T05_GetOutletById.getOutletById)
      .pause(3)
      .exec(AccountManagementScripts.T06_MyOrders.myOrders)
      .pause(3)
      .exec(AccountManagementScripts.T07_CreateWishlist.createWishlist)
      .pause(3)
      .exec(AccountManagementScripts.T08_GetWishlists.getWishlists)
      .pause(3)
      .exec(AccountManagementScripts.T09_UpdateWishlist.updateWishlist)
      .pause(3)
//      .exec(AccountManagementScripts.T10_PaginatedListOfAllProductsInWishlist.PaginatedList)
//      .pause(3)
      .exec(AccountManagementScripts.T11_RemoveProductFromWishlist.removeProductFromWishlist)
      .pause(3)
      .exec(AccountManagementScripts.T12_DeleteWishlist.DeleteWishlist)
      .pause(3)


}
    .exec(CustomerOnboarding.T12_RevokeToken.revokeToken)
}
