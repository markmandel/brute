(ns brute.entity_test
    (:import (java.util UUID)
             (clojure.lang PersistentArrayMap))
    (:use [midje.sweet]
          [brute.entity]
          [clojure.pprint :only [pprint]]))

(def system (atom 0))

(defn- setup!
    "Provides setup for the tests. Has side effects"
    []
    (reset! system (create-system)))

(defn- r! [s] (reset! system s))

(namespace-state-changes (before :facts (setup!)))

(defrecord Position [x y])
(defrecord Velocity [x y])

(defmethod get-component-type PersistentArrayMap
           [component]
    (:type component))

(fact "The Entity I create is a unique uuid"
      (let [uuid (create-entity)]
          uuid => truthy
          (> (-> uuid .toString .length) 0) => true
          (class uuid) => UUID
          (create-entity) =not=> uuid))

(fact "Creating and adding an entity results in it being added to the global list"
      (let [entity (create-entity)]
          (-> @system
              (add-entity entity)
              (get-all-entities)) => [entity]))

(fact "By default, a component returns it's class as it's type"
      (let [pos (->Position 5 5)]
          (get-component-type pos) => (class pos)))

(fact "We can extend the component type system, through the multimethod"
      (let [pos {:type :position :x 5 :y 5}]
          (get-component-type pos) => :position))

(fact "You can add a component instance to an entity, and then retrieve it again"
      (let [entity (create-entity)
            pos (->Position 5 5)]
          (-> @system
              (add-entity entity)
              (add-component entity pos)
              (get-component entity Position)) => pos))

(fact "You can add a component instance to an entity, and then overwrite it with another component of the same type"
      (let [entity (create-entity)
            pos (->Position 5 5)
            pos2 (->Position 10 10)]
          (-> @system
              (add-entity entity)
              (add-component entity pos)
              r!
              (get-component entity Position)) => pos
          (-> @system
              (add-component entity pos2)
              (get-component entity Position)) => pos2))

(fact "You can add an extended component instance to an entity, and then retrieve it again"
      (let [entity (create-entity)
            pos {:type :position :x 5 :y 5}]
          (-> @system
              (add-entity entity)
              (add-component entity pos)
              (get-component entity :position)) => pos))

(fact "If an entity doesn't have a component, it should return nil"
      (let [entity (create-entity)
            pos (->Position 5 5)]
          (-> @system
              (add-entity entity)
              (get-component entity Position)) => falsey
          (-> @system
              (add-component entity pos)
              (get-component entity Velocity)) => falsey))

(fact "Can retrieve all entites that have a single type"
      (get-all-entities-with-component @system Position) => []
      (let [entity1 (create-entity)
            entity2 (create-entity)
            pos (->Position 5 5)]
          (-> @system
              (add-entity entity1)
              (add-entity entity2)
              (add-component entity1 pos)
              (add-component entity2 pos)
              (get-all-entities-with-component Position)) => (just #{entity1, entity2})))

(fact "Can retrieve all entites that have a single extended type"
      (get-all-entities-with-component @system :position) => []
      (let [entity1 (create-entity)
            entity2 (create-entity)
            pos {:type :position :x 5 :y 5}]
          (-> @system
              (add-entity entity1)
              (add-entity entity2)
              (add-component entity1 pos)
              (add-component entity2 pos)
              (get-all-entities-with-component :position)) => (just #{entity1, entity2})))

(fact "Are able to removing an entity's component"
      (let [entity (create-entity)
            pos (->Position 5 5)
            vel (->Velocity 10 10)]
          (-> @system
              (add-entity entity)
              (add-component entity pos)
              (add-component entity vel)
              r!)

          (get-component @system entity Position) => truthy
          (get-component @system entity Velocity) => truthy
          (get-all-entities-with-component @system Position) => [entity]
          (get-all-entities-with-component @system Velocity) => [entity]

          (-> @system (remove-component entity pos) r!)

          (get-component @system entity Position) => nil
          (get-component @system entity Velocity) => truthy
          (get-all-entities-with-component @system Position) => []
          (get-all-entities-with-component @system Velocity) => [entity]

          (-> @system (remove-component entity vel) r!)

          (get-component @system entity Position) => nil
          (get-component @system entity Velocity) => nil
          (get-all-entities-with-component @system Position) => []
          (get-all-entities-with-component @system Velocity) => []))

(fact "You can kill an entity, and it goes bye bye"
      (let [entity (create-entity)
            pos (->Position 5 5)
            vel (->Velocity 10 10)]

          (-> @system
              (add-entity entity)
              (add-component entity pos)
              (add-component entity vel)
              r!
              (get-all-entities)) => [entity]

          (-> @system
              (kill-entity entity)
              r!)

          (get-all-entities @system) => []
          (get-component @system entity Position) => nil
          (get-component @system entity Velocity) => nil))

(fact "You can get all the components on a single entity, if you so choose"
      (let [entity (create-entity)
            pos (->Position 5 5)
            vel (->Velocity 10 10)]
          (-> @system
              (add-entity entity)
              r!
              (get-all-components-on-entity entity)) => []
          (-> @system
              (add-component entity pos)
              r!
              (get-all-components-on-entity entity)) => (just #{pos})
          (-> @system
              (add-component entity vel)
              r!
              (get-all-components-on-entity entity)) => (just #{pos vel})
          (-> @system
              (kill-entity entity)
              (get-all-components-on-entity entity)) => []))

(fact "You can update a component by applying a function and parameters to it, like update-in does"
      (let [entity (create-entity)
            pos (->Position 5 5)]
          (-> @system
              (add-entity entity)
              (add-component entity pos)
              r!)
          (:x (get-component @system entity Position)) => 5
          (-> @system
              (update-component entity Position assoc :x 10)
              r!)
          (:x (get-component @system entity Position)) => 10

          ;; send the same thing again, should be the same on the other side
          (-> @system
              (update-component entity Position assoc :x 10)
              r!)
          (:x (get-component @system entity Position)) => 10

          (-> @system
              (update-component entity Position (fn [_] nil))
              r!)

          (:x (get-component @system entity Position)) => 10))