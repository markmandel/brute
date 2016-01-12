# Brute

A simple and lightweight Entity Component System library for writing games with Clojure or ClojureScript.

![Clojars Version](https://clojars.org/brute/latest-version.svg?v=3)

[![wercker status](https://app.wercker.com/status/5f5d692036ee110c41a50ccc7b6f4ae5/m "wercker status")](https://app.wercker.com/project/bykey/5f5d692036ee110c41a50ccc7b6f4ae5)

The aim of this project was to use basic Clojure building blocks to form an Entity System architecture, and get out of the
author's way when deciding exactly what approach would best fit their game when integrating with this library.

To that end:

- Entities are UUIDs.
- The Component type system can be easily extended through a multimethod `get-component-type`, but defaults to using the component's instance class as its type.
- Components can therefore be defrecords or deftypes by default, but could easily be maps or just about anything else.
- Systems are simply references to functions of the format `(fn [delta])`.

To learn more about Entity Component Systems, please read the [Entity Systems Wiki](http://entity-systems.wikidot.com/).
I personally, also found [Adam Martin's Blog Post series](http://t-machine.org/index.php/2007/09/03/entity-systems-are-the-future-of-mmog-development-part-1/)
very useful at giving a step by step explanation of Entity System architecture.

## News

Blog posts and news can be found on the [Compound Theory Blog](http://www.compoundtheory.com/category/brute)

## Usage

See the [Library API](https://markmandel.github.io/brute/codox/) for all the functionality of this library.

### Quick Start

A quick example based overview of what functionality Brute provides.

I've used fully qualified namespace, *brute.entity* and *brute.system* to be explicit about what is part of Brute in the demo code below, and what denotes custom code.

#### Creating the Basic Entity Component System

Brute doesn't store any data in a ref/atom, but instead provides you with the functions and capabilities for manipulating an immutable data structure that represents this ES system.  This is particularly useful because:

- How the entity data structure is persisted is up to you and the library you are using (although 9/10 times I expect it will end up stored in a single atom, and reset! on each game loop), which gives you complete control over when state mutation occurs – if it occurs at all. This makes concurrent processes much simpler to develop.
- You get direct access to the ES data structure, in case you want to do something with it that isn’t exposed in the current API.
- You can easily have multiple ES systems within a single game, e.g. for sub-games.
- Saving a game becomes simple: Just serialise the ES data structure and store. Deserialise to load.
- Basically all the good stuff having immutable data structures and pure functions should give you.

To create the initial system data structure:

```clojure
(brute.entity/create-system)
```

This is actually a map, that lets you access Entities and their Components from a variety of ways, so you can always do it in a performant way.

```clojure
    {;; Nested Map of Component Types -> Entity -> Component Instance
        :entity-components      {}
     ;; Map of Entities -> Set of Component Types
        :entity-component-types {}}
```

Do note, that this data structure may be subject to change between releases.

#### Creating a Ball Entity, with corresponding Component instances.

- A `Ball` component instance to know it is a Ball.
- A `Rectangle` component instance to draw a rectangle in its' place
- A `Velocity` component instance to know what direction it is travelling in, and how fast.

```clojure
(defn create-ball
    "Creates a ball entity"
    [system]
    (let [ball (brute.entity/create-entity) ;; Returns a UUID for the Entity
          center-x (-> (graphics! :get-width) (/ 2) (m/round))
          center-y (-> (graphics! :get-height) (/ 2) (m/round))
          ball-size 20
          ball-center-x (- center-x (/ ball-size 2))
          ball-center-y (- center-y (/ ball-size 2))
          angle (create-random-angle)]
        (-> system
            (brute.entity/add-entity ball) ;; Adds the entity to the ES data structure and returns it
            (brute.entity/add-component ball (c/->Ball)) ;; Adds the Ball instance to the ES data structure and returns it
            (brute.entity/add-component ball (c/->Rectangle (rectangle ball-center-x ball-center-y ball-size ball-size) (color :white))) ;; Adds the Rectangle instance to the ES data structure and returns it
            (brute.entity/add-component ball (c/->Velocity (vector-2 0 300 :set-angle angle)))))) ;; Adds the Velocity instance to the ES data structure and returns it
```

#### Render each of the Entities that have a Rectangle Component

```clojure
(defn- render-rectangles
    "Render all the rectangles"
    [system]
    (let [shape-renderer (:shape-renderer (:renderer system))]
        (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
        (doseq [entity (brute.entity/get-all-entities-with-component system Rectangle)] ;; loop around all the entities that have a Rectangle Component instance
            (let [rect (brute.entity/get-component system entity Rectangle) ;; get the Rectangle Component Instance for this entity
                  geom (:rect rect)] ;; Rectangle component contains a Rectangle geometry shape.
                (doto shape-renderer ;; Draw the actual rectangle on the screen
                    (.setColor (:colour rect)) ;; Rectangle component contains the colour
                    (.rect (rectangle! geom :get-x)
                           (rectangle! geom :get-y)
                           (rectangle! geom :get-width)
                           (rectangle! geom :get-height)))))
        (.end shape-renderer)))
```

#### Systems Management
System management is an optional feature for you to use with Brute.

The following adds each system function to a list contains on the Entity System data structure, maintaining the order in which they were added.

```clojure
(defn- create-systems
    "register all the system functions"
    [system]
    (-> system
    	(brute.system/add-system-fn input/process-one-game-tick)
    	(brute.system/add-system-fn scoring/process-one-game-tick)
    	(brute.system/add-system-fn ai/process-one-game-tick)
    	(brute.system/add-system-fn physics/process-one-game-tick)
    	(brute.system/add-system-fn rendering/process-one-game-tick)))
```

Finally call each function in the order added, simply write:

```clojure
(brute.system/process-one-game-tick system (graphics! :get-delta-time))
```



## Game Examples

- [Pong Clone](https://github.com/markmandel/brute-play-pong) written with [play-clj](https://github.com/oakes/play-clj)

## Contributing

Pull requests are always welcome!

Active development happens on the `develop` branch. The `master` branch is the source for the current release.

### Reader Conditionals
This project uses [Reader Conditionals](http://clojure.org/reader#The%20Reader--Reader%20Conditionals) to support both Clojure and ClojureScript. It should be a seamless experience.

## Testing

To test under Clojure: `lein test`

To test under ClojureScript: `lein cljstest`

To run all tests: `lein alltest`

### Run all tests in the a Docker Container
You should be able to run all the tests without having to install anything, except to pull the Docker container.

`make test` will run all the tests in the development Docker container, which should make development easier.

## License

Copyright © 2016 Mark Mandel, Google Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
