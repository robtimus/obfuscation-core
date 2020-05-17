# obfuscation-core

Provides functionality for obfuscating text. This can be useful for logging information that contains sensitive information.

Besides providing a framework that can be extended to provide simple or complex obfuscators, this library comes with a small set of predefined obfuscators.
Most can be found through factory methods on class [Obfuscator](https://robtimus.github.io/obfuscation-core/apidocs/com/github/robtimus/obfuscation/Obfuscator.html).
See [Examples](https://robtimus.github.io/obfuscation-core/examples.html) for more information.

## Streaming obfuscation

An `Obfuscator` has method `streamTo` which takes a `StringBuilder`, `Writer` or other `Appendable`, and returns a `Writer` that will obfuscate the written text automatically.

## Preventing leaking string representations

The following methods of `Obfuscator` can be used to help prevent accidentally leaking string representations of objects, for instance by logging them:

* `obfuscateList`, `obfuscateSet` and `obfuscateCollection` create `List`, `Set` and `Collection` decorators respectively that obfuscate separate elements when calling `toString()`.
* `obfuscateMap` creates a `Map` decorator that obfuscates separate values (but not keys) when calling `toString()`.
* `obfuscateObject` creates a wrapper around an existing object that obfuscates the object when calling `toString()`.

## Obfuscating complex structures

Besides obfuscating simple text, it's possible to obfuscate complex structures using the following classes and libraries:

* [MapObfuscator](https://robtimus.github.io/obfuscation-core/apidocs/com/github/robtimus/obfuscation/MapObfuscator.html) can obfuscate maps by providing a separate `Obfuscator` for each entry. Entries can even have no obfuscation at all.
* [PropertiesObfuscator](https://robtimus.github.io/obfuscation-core/apidocs/com/github/robtimus/obfuscation/PropertiesObfuscator.html) can obfuscate `Properties` objects by providing a separate `Obfuscator` for each property. Properties can even have no obfuscation at all.
* [obfuscation-commons-lang](https://robtimus.github.io/obfuscation-commons-lang/) provides extensions to [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/) for obfuscating objects.
* [obfuscation-http](https://robtimus.github.io/obfuscation-http/) provides support for obfuscating HTTP requests and responses.
* [obfuscation-jackson](https://robtimus.github.io/obfuscation-jackson/) and [obfuscation-json](https://robtimus.github.io/obfuscation-json/) provide support for obfuscating properties in JSON documents.
* [obfuscation-yaml](https://robtimus.github.io/obfuscation-yaml/) provides support for obfuscating properties in YAML documents.

## Writing custom obfucators

Package [com.github.robtimus.obfuscation.support](https://robtimus.github.io/obfuscation-core/apidocs/com/github/robtimus/obfuscation/support/package-summary.html) provides several classes that can be used to create custom obfuscators.

## Extensions

* [obfuscation-annotations](https://robtimus.github.io/obfuscation-annotations/) provides general purpose `Obfuscator` annotations.
* [obfuscation-jackson-databind](https://robtimus.github.io/obfuscation-jackson-databind/) provides integration with [jackson-databind](https://github.com/FasterXML/jackson-databind).
