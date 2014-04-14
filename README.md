# Brute

A simple and lightweight Entity Component System library for writing games with Clojure.

![Clojars Version](https://clojars.org/brute/latest-version.svg?v=1)

The aim of this project was to use basic Clojure building blocks to form an Entity System architecture, and get out of the
author's way when deciding exactly what approach would best fit their game when integrating with this library.

- Entities are UUIDs.
- The Component type system can be easily extended through a multimethod `get-component-type`.
- Components can therefore be defrecords, deftypes, maps or really anything you choose, really.
- Systems are simply references to functions of the format `(fn [delta])`.

To learn more about Entity Component Systems, please read the [Entity Systems Wiki](http://entity-systems.wikidot.com/).
I personally, also found [Adam Martin's Blog Post series](http://t-machine.org/index.php/2007/09/03/entity-systems-are-the-future-of-mmog-development-part-1/)
very useful at giving a step by step explanation of Entity System architecture.

## Usage

See the [Library API](https://markmandel.github.io/brute/codox/) for all the functionality of this library.

### Quick Start

A quick example based overview of what functionality Brute provides.

I've used fully qualified namespace, *brute.entity* and *brute.system* to be explicit about what is part of Brute in the demo code
below, and what denotes custom code.

#### Creating a Ball entity, with corresponding components.

- A `Ball` component instance to know it is a Ball.
- A `Rectangle` component instance to draw a rectangle in its' place
- A `Velocity` component instance to know what direction it is travelling in, and how fast.

```clojure
(defn create-ball
    "Creates a ball entity"
    []
    (let [ball (brute.entity/create-entity!) ;;create the ball entity
          center-x (-> (graphics! :get-width) (/ 2) (m/round))
          center-y (-> (graphics! :get-width) (/ 2) (m/round))
          ball-size 20
          ball-center-x (- center-x (/ ball-size 2))
          ball-center-y (- center-y (/ ball-size 2))
          angle (create-random-angle)]

        (brute.entity/add-component! ball (c/->Ball)) ;; Add Ball Component Instance
        (brute.entity/add-component! ball (c/->Rectangle (rectangle ball-center-x ball-center-y ball-size ball-size) (color :white))) ;; Add Rectangle Component Instance
        (brute.entity/add-component! ball (c/->Velocity (vector-2 0 300 :set-angle angle))) ;; Add Velocity instance
        ))
```

#### Render each of the Entities that have a Rectangle Component

```clojure
(defn- render-rectangles
    "Render all the rectangles"
    []
    (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
    (doseq [entity (brute.entity/get-all-entities-with-component Rectangle)] ;; loop around all the entities that have a Rectangle Component instance
        (let [rect (brute.entity/get-component entity Rectangle) ;; get the Rectangle Component Instance for this entity
              colour (:colour rect) ;; Rectangle component contains the colour
              geom (:rect rect)] ;; Rectangle component also contains a Rectangle geometry shape.
            (doto shape-renderer ;; draw the actual rectangle on the screen
                (.setColor (:colour rect))
                (.rect (rectangle! geom :get-x)
                       (rectangle! geom :get-y)
                       (rectangle! geom :get-width)
                       (rectangle! geom :get-height)))))
    (.end shape-renderer))
```

#### Systems Management
Adds each system function to a list, in the order that they are added

```clojure
(defn- create-systems
    "register all the system functions"
    []
    (brute.system/add-system-fn! scoring/process-one-game-tick)
    (brute.system/add-system-fn! input/process-one-game-tick)
    (brute.system/add-system-fn! ai/process-one-game-tick)
    (brute.system/add-system-fn! physics/process-one-game-tick)
    (brute.system/add-system-fn! rendering/process-one-game-tick))

```

Finally call each function in order added, simple write:

```clojure
(brute.system/process-one-game-tick (graphics! :get-delta-time))
```



## Game Examples

- [Pong Clone](https://github.com/markmandel/brute-play-pong) written with [play-clj](https://github.com/oakes/play-clj)

## License

Copyright © 2014 Mark Mandel

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
