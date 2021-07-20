package modules

import controllers.ExampleController
import scaldi.Module

class ApplicationModule extends Module {

  bind[ExampleController] to injected[ExampleController]
}
