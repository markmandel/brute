(ns brute.core-test
    (:import (java.util UUID))
    (:use [midje.sweet]
          [brute.core]))

(defn- setup!
    "Provides setup for the tests. Has side effects"
    []
    (reset-all!))

(namespace-state-changes (before :facts (setup!)))

(defrecord Position [x y])
(defrecord Velocity [x y])

(fact "The Entity I create is a unique uuid"
      (let [uuid (create-entity!)]
          uuid => truthy
          (> (-> uuid .toString .length) 0) => true
          (class uuid) => UUID
          (create-entity!) =not=> uuid))

(fact "Creating an entity results in it being added to the global list"
      (let [uuid (create-entity!)]
          [uuid] => (get-all-entities)))

(fact "By default, a component returns it's class as it's type"
      (let [pos (->Position 5 5)]
          (get-component-type pos) => (class pos)))

(fact "You can add a component instance to an entity, and then retrieve it again"
      (let [entity (create-entity!)
            pos (->Position 5 5)]
          (add-component! entity pos)
          (get-component entity Position) => pos))

(fact "If an entity doesn't have a component, it should return nil"
      (let [entity (create-entity!)
            pos (->Position 5 5)]
          (get-component entity Position) => falsey
          (add-component! entity pos)
          (get-component entity Velocity) => falsey))

(fact "Can retrieve all entites that have a single type"
      (get-all-entities-with-component Position) => []
      (let [entity1 (create-entity!)
            entity2 (create-entity!)
            pos (->Position 5 5)]
          (add-component! entity1 pos)
          (add-component! entity2 pos)
          (get-all-entities-with-component Position) => (just #{entity1, entity2})))

(fact "Are able to removing an entity's component"
      (let [entity (create-entity!)
            pos (->Position 5 5)
            vel (->Velocity 10 10)]
          (add-component! entity pos)
          (add-component! entity vel)

          (get-component entity Position) => truthy
          (get-component entity Velocity) => truthy
          (get-all-entities-with-component Position) => [entity]
          (get-all-entities-with-component Velocity) => [entity]

          (remove-component! entity pos)

          (get-component entity Position) => nil
          (get-component entity Velocity) => truthy
          (get-all-entities-with-component Position) => []
          (get-all-entities-with-component Velocity) => [entity]

          (remove-component! entity vel)

          (get-component entity Position) => nil
          (get-component entity Velocity) => nil
          (get-all-entities-with-component Position) => []
          (get-all-entities-with-component Velocity) => []
          )
      )