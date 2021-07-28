
package scommons.sbtplugin.test

import scala.scalajs.js.annotation.JSExport

object Main {

  @JSExport
  def main(args: Array[String]): Unit = {
    println(s"test: ${MainCss.test}, test_btn: ${MainCss.test_btn}")
  }
}
