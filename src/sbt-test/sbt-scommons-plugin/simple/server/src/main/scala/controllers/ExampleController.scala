package controllers

import play.api.mvc.{AbstractController, Action, ControllerComponents}
import play.twirl.api.StringInterpolation

class ExampleController(components: ControllerComponents) extends AbstractController(components) {

  val index = {
    val scriptUrl = bundleUrl("client")
    val result =
      Ok(
        html"""<!doctype html>
        <html>
          <head></head>
          <body>
            <div>App is not loaded.</div>
            <script src="$scriptUrl"></script>
          </body>
        </html>
      """
      )
    Action(result)
  }

  def bundleUrl(projectName: String): Option[String] = {
    val name = projectName.toLowerCase
    Seq(s"$name-opt-bundle.js", s"$name-fastopt-bundle.js")
      .find(name => getClass.getResource(s"/public/$name") != null)
      .map(controllers.routes.Assets.versioned(_).url)
  }
}
