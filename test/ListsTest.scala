import com.google.appengine.tools.development.testing.{LocalDatastoreServiceTestConfig, LocalUserServiceTestConfig, LocalServiceTestHelper}
import org.junit._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSuite, BeforeAndAfterEach}
import play.test._
import play.mvc._
import play.mvc.Http._
import models._

import collection.JavaConversions._

class ListsTest extends FunctionalTest with FunSuite with ShouldMatchers with BeforeAndAfterEach with Browser {
  /**
   * this emulates user login
   * see: http://code.google.com/intl/ja/appengine/docs/java/tools/localunittesting.html#Writing_Authentication_Tests
   */
  private val helper: LocalServiceTestHelper =
          new LocalServiceTestHelper(new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig())
              .setEnvIsAdmin(true).setEnvEmail("foo@bar.com").setEnvAuthDomain("http://localhost")


  /**
   * I didn't really want to write <code>getContent(response) should be ("contentExpected")</code>...
   */
  implicit def makeResponseRicher(response: Response) = RichResponse(response)

  case class RichResponse(response: Response) {
    def content: String = getContent(response)
  }

  override def beforeEach() {
    helper.setEnvIsLoggedIn(false)
    setUpLocalServices
  }

  override def afterEach() {
    helper.tearDown

    clearCookies
  }

  private def setUpLocalServices {
    helper.setUp

    val userEmail = "foo@bar.com"
    val exampleList = new List(userEmail, "exampleList")
    val exampleItem = new Item(exampleList, "exampleItem")

    exampleList.insert
    exampleItem.insert
  }

  private def withLoggedIn[T](tests: => T) {
    helper.tearDown
    helper.setEnvIsLoggedIn(true)
    setUpLocalServices

    tests
  }

  test("Lists.index when logged in") {
    withLoggedIn {
      val response = GET("/lists")

      response.status should be (200)
      response.content should include ("exampleList")
    }
  }

  test("Lists.index when not logged in") {
    val response = GET("/lists")

    response.status should be (302)
  }

  test("Lists.show") {
    withLoggedIn {
      val exampleList = ListOp.all.fetch().get(0)
      val response = GET(newRequest, "/lists/" + exampleList.id)

      response.content should include ("exampleItem")
    }
  }
}