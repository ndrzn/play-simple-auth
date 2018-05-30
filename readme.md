# Simple Play Auth

Play 2.6.x+

## Usage

1. Add implementation of `AuthConfiguration` trait, or use `DefaultAuthConfiguration` class

2. Create custom action with your own auth rules, check `sample` folder for an example.

3. Use new action in any controller:

```scala

class Mycontroller @Inject()(authAction: MyAuthAction, ...) {

    def endpoint() = authAction(<permissions>) { implicit request =>
      Ok  
    }
} 

```
