# Brute

A simple and lightweight Entity Component System library for writing games with Clojure.

The aim of this project was to use basic Clojure building blocks to form an ECS architecture.

- Entities are UUIDs.
- The Component type system can be easily extended through a multimethod `get-component-type`.
- Components can therefore be defrecords, deftypes, maps or really anything you choose, really.
- Systems are simply references to functions of the format `(fn [delta])`.

## Usage

More to come...

## License

Copyright © 2014 Mark Mandel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.