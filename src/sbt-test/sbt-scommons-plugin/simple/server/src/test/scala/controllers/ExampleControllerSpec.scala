package controllers

import org.scalatest.{DoNotDiscover, OptionValues}
import org.scalatestplus.play._

@DoNotDiscover
class ExampleControllerSpec extends BaseControllersSpec
  with OptionValues
  with OneBrowserPerSuite
  with HtmlUnitFactory {

  it should "load assets generated by webpack" in {
    //when
    go to s"$baseUrl/assets/styles/styles.css"

    //then
    pageSource should include ("background: url(data:image/png;base64")
  }

  it should "load the page without error" in {
    //when
    go to s"$baseUrl/index.html"

    //then
    //find(tagName("p")).value.text mustBe "Hello, world!"
  }
}
