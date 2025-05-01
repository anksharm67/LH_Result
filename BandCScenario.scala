package scenario

import APIScripts.BandC.BasketandCheckout
import APIScripts.Token
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object BandCScenario {

  val salesforceURL = "https://carlsbergiit--devint.sandbox.my.salesforce.com"

  val siteURL = "https://carlsbergiit--test.sandbox.my.site.com"

  //  val baseURL = "https://lao-dev.1901.carlsberg.com"

  val baseURL = "https://lao-sit.1901.carlsberg.com"

  val jwttokenurl = "https://carlsbergemeab2cdev.b2clogin.com"


  val P_SFtoken = "00DUC000001URLZ!AQEAQJ2j7vfW2FS9Q3CmCSfjTQgA6VJMS0lyb6c7gqaoR0or46gRNBlQN7Qf9u73jwRWK.ObTBefIvCnA9FIxOugrGpjrqME"

  val P_adminToken = "00DUD000002STvk!AQEAQJOqJGBbnm3ueLrmNbZ6NsAlWPBY4BNMTRUV5RoTcvOvBGy_oY2rA9cQN7rHbgmjjW4USNKTJdlyMBKZwOH8VbDm4ijZ"

  val P_token = "00DUC000002HTIz!AQEAQAhHpKXXivXaUX5EVtn3vhnerNuTGu5IB8gCEVXZ4cJM9CRyjHCe4QUmUy1AbRA8vEG9hgXVnEQb_5UzdNcUD.JnMthc"


  val P_Ocp_Apim_Subscription_Key = "3546681d2c8941b5babc440334091be7"

  val P_BnC_Cookie = "ASLBSA=000377134ec050e1bc091ae70b8acc33dee9b5b2ae1b78dc406d2250e80ea7389590; ASLBSACORS=000377134ec050e1bc091ae70b8acc33dee9b5b2ae1b78dc406d2250e80ea7389590"

  val P_Token_Cookie = "BrowserId=POhd6XTZEe2v6_8M7WG26g"



  val testUsersCredentials_feed = csv("data/Carlsberg_API_TestUserCredentials.csv").circular

  //  val distributors_feed = csv("data/Carlsberg_API_Distributors.csv").circular

  val products_feed = csv("data/Carlsberg_API_Products.csv").circular

  val dates_feed = csv("data/Carlsberg_API_DeliveryDates.csv").circular


  val SC02_BasketandCheckout_Scenario = scenario("SC03_CB_Laos_B&C")

    .exec(_.set("Scn_ID", "SC03_CB_Laos_B&C"))
    .exec(_.set("salesforceURL",salesforceURL))
    .exec(_.set("siteURL",siteURL))
    .exec(_.set("baseURL",baseURL))
    .exec(_.set("jwttokenurl",jwttokenurl))
    .feed(testUsersCredentials_feed)
    .exec(Token.LoginPage.LoginPage()).pause(5)
    .exec(Token.JWTtoken.Generate_JWTtoken()).pause(5)
    .exec(Token.AccessToken.Generate_AccessToken()).pause(5)

    .exec(session => {
      // Store the access token timestamp in seconds
      session.set("lastTokenTime", System.currentTimeMillis() / 1000)
    })


    //    .repeat(5) {
    .forever {

      tryMax(1) {
        exec(flushSessionCookies)
          .exec(flushCookieJar)
          .exec(flushHttpCache)
          .exec(_.set("Scn_ID", "SC03_CB_Laos_B&C"))
          .exec(_.set("salesforceURL",salesforceURL))
          .exec(_.set("siteURL",siteURL))
          .exec(_.set("baseURL",baseURL))
          .exec(_.set("jwttokenurl",jwttokenurl))
          .exec(_.set("P_SFtoken",P_SFtoken))
          .exec(_.set("P_adminToken",P_adminToken))
          .exec(_.set("P_token",P_token))
          .exec(_.set("P_Ocp_Apim_Subscription_Key",P_Ocp_Apim_Subscription_Key))
          .exec(_.set("P_BnC_Cookie",P_BnC_Cookie))
          .exec(_.set("P_Token_Cookie",P_Token_Cookie))

          //        .feed(testUsersCredentials_feed)
          //        .feed(distributors_feed)
          .feed(products_feed)
          .feed(dates_feed)



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

            exec(Token.LoginPage.LoginPage()).pause(5)
              .exec(Token.JWTtoken.Generate_JWTtoken()).pause(5)
              .exec(Token.AccessToken.Generate_AccessToken()).pause(5)

              .exec(session => session.set("lastTokenTime", System.currentTimeMillis() / 1000))
          }


          .exec(BasketandCheckout.getRandom5DigitCartNo())
          .exec(BasketandCheckout.createCart).pause(5)
          .exec(BasketandCheckout.addCart).pause(5)
          .exec(BasketandCheckout.getRandom1DigitCartQuantity())
          .exec(BasketandCheckout.amendCart).pause(5)
          .exec(BasketandCheckout.getCart).pause(5)
          .exec(BasketandCheckout.deleteCartItem).pause(5)
          .exec(BasketandCheckout.addCart).pause(5)
          .exec(BasketandCheckout.clearCart).pause(5)
          .exec(BasketandCheckout.addCart).pause(5)
          .exec(BasketandCheckout.deleteCart).pause(5)


          .exec(BasketandCheckout.createCart).pause(5)
          .exec(BasketandCheckout.addCart).pause(5)
          .exec(BasketandCheckout.amendCart).pause(5)
          .exec(BasketandCheckout.createCheckout).pause(5)
          .exec(BasketandCheckout.getCheckout).pause(5)
          .exec(BasketandCheckout.updateCheckout).pause(5)
          .exec(BasketandCheckout.placeOrder).pause(5)
          .exec(BasketandCheckout.getOrderSummary).pause(5)
          .exec(BasketandCheckout.getOrderHistory).pause(5)
          .exec(BasketandCheckout.cancelOrder).pause(5)


      }
    }

}
