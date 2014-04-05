(ns brute.entity_test
    (:import (java.util UUID)
             (clojure.lang PersistentArrayMap))
    (:use [midje.sweet]
          [brute.entity]))

(defn- setup!
    "Provides setup for the tests. Has side effects"
    []
    (reset-all!))

(namespace-state-changes (before :facts (setup!)))

(defrecord Position [x y])
(defrecord Velocity [x y])

(defmethod get-component-type PersistentArrayMap
           [component]
    (:type component))

(fact "The Entity I create is a unique uuid"
      (let [uuid (create-entity!)]
          uuid => truthy
          (> (-> uuid .toString .length) 0) => true
          (class uuid) => UUID
          (create-entity!) =not=> uuid))

(fact "Creating an entity results in it being added to the global list"
      (let [entity (create-entity!)]
          (get-all-entities) => #{entity}))

(fact "By default, a component returns it's class as it's type"
      (let [pos (->Position 5 5)]
          (get-component-type pos) => (class pos)))

(fact "We can extend the component type system, through the multimethod"
      (let [pos {:type :position :x 5 :y 5}]
          (get-component-type pos) => :position))

(fact "You can add a component instance to an entity, and then retrieve it again"
      (let [entity (create-entity!)
            pos (->Position 5 5)]
          (add-component! entity pos)
          (get-component entity Position) => pos))

(fact "You can add an extended component instance to an entity, and then retrieve it again"
      (let [entity (create-entity!)
            pos {:type :position :x 5 :y 5}]
          (add-component! entity pos)
          (get-component entity :position) => pos))

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

(fact "Can retrieve all entites that have a single extended type"
      (get-all-entities-with-component :position) => []
      (let [entity1 (create-entity!)
            entity2 (create-entity!)
            pos {:type :position :x 5 :y 5}]
          (add-component! entity1 pos)
          (add-component! entity2 pos)
          (get-all-entities-with-component :position) => (just #{entity1, entity2})))

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
          (get-all-entities-with-component Velocity) => []))

(fact "You can kill an entity, and it goes bye bye"
      (let [entity (create-entity!)
            pos (->Position 5 5)
            vel (->Velocity 10 10)]
          (add-component! entity pos)
          (add-component! entity vel)
          (get-all-entities) => #{entity}

          (kill-entity! entity)

          (get-all-entities) => #{}
          (get-component entity Position) => nil
          (get-component entity Velocity) => nil))

(fact "You can get all the components on a single entity, if you so choose"
      (let [entity (create-entity!)
            pos (->Position 5 5)
            vel (->Velocity 10 10)]

          (get-all-components-on-entity entity) => []

          (add-component! entity pos)
          (get-all-components-on-entity entity) => (just #{pos})

          (add-component! entity vel)
          (get-all-components-on-entity entity) => (just #{pos vel})

          (kill-entity! entity)
          (get-all-components-on-entity entity) => []))

