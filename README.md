# copr-scala

This is a client-side library which provides interaction with the
[Fedora Copr](http://copr.fedoraproject.org/) system.

It was originally written for
[Fedora Mobile](https://github.com/fedora-infra/mobile), but isn't tied to any
specific usecase.

It makes heavy use of the Argonaut library and scalaz.

## Using it

```scala
resolvers += "relrod @ FedoraPeople" at "http://codeblock.fedorapeople.org/maven/"

libraryDependencies += "org.fedoraproject" %% "coprscala" % "0.0.1"
```

# License

Apache 2.
