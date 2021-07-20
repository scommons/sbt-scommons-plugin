package controllers

import org.scalatest.{Suites, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, OptionalDevContext}
import scaldi.Module
import scaldi.play.ScaldiApplicationBuilder

class ControllersSpec extends Suites(
  new ExampleControllerSpec
) with TestSuite
  with GuiceOneServerPerSuite {

  implicit override lazy val app: Application = new ScaldiApplicationBuilder(modules = List(new Module {
    bind[OptionalDevContext] to new OptionalDevContext(None)
  })).build()
}
