(ns brute.entity-test
  "Tests for the entity management functions"
  #?(:clj
     (:import (java.util UUID)
              (clojure.lang PersistentArrayMap)))
  (:require [brute.entity :refer [create-system
                                  create-entity
                                  add-entity
                                  get-all-entities
                                  get-component-type
                                  add-component
                                  get-component
                                  update-component
                                  get-all-entities-with-component
                                  remove-component
                                  kill-entity
                                  get-all-components-on-entity]]
    #?(:clj
            [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest is use-fixtures]])))

(def system (atom 0))
(defrecord Position [x y])
(defrecord Velocity [x y])

(defn- setup!
  "Provides setup for the tests. Has side effects"
  [f]
  (reset! system (create-system))
  (f))

(defn- r! [s] (reset! system s))

(use-fixtures :each setup!)

(defmethod get-component-type PersistentArrayMap
  [component]
  (:type component))

;; The Entity I create is a unique uuid
(deftest entity-unique-uuid
  (let [uuid (create-entity)]
    (is uuid)
    #?(:clj  (do (is (= (-> uuid .toString .length) 36))
                 (is (= (class uuid) UUID)))
       :cljs (do (is (= (-> uuid .toString .-length) 36))
                 (is (= (type uuid) UUID))))
    (is (not= (create-entity) uuid))))

;; Creating and adding an entity results in it being added to the global list
(deftest global-entity-list
  (let [entity (create-entity)]
    (is (= (-> @system
               (add-entity entity)
               (get-all-entities)) [entity]))))

;; By default, a component returns it's class as it's type
(deftest default-class-as-component-type
  (let [pos (->Position 5 5)]
    (is (= (get-component-type pos) (#?(:clj class :cljs type) pos)))))


;; We can extend the component type system, through the multimethod
(deftest extend-component-type-multimethod
  (let [pos {:type :position :x 5 :y 5}]
    (is (= (get-component-type pos) :position))))

;; You can add a component instance to an entity, and then retrieve it again
(deftest add-then-get-component
  (let [entity (create-entity)
        pos (->Position 5 5)]
    (is (= (-> @system
               (add-entity entity)
               (add-component entity pos)
               (get-component entity Position)) pos))))


;; You can add a component instance to an entity, and then overwrite it with another component of the same type
(deftest overwrite-component-of-same-type
  (let [entity (create-entity)
        pos (->Position 5 5)
        pos2 (->Position 10 10)]
    (is (= (-> @system
               (add-entity entity)
               (add-component entity pos)
               r!
               (get-component entity Position)) pos))
    (is (= (-> @system
               (add-component entity pos2)
               (get-component entity Position)) pos2))))

;; You can add an extended component instance to an entity, and then retrieve it again
(deftest add-then-get-extended-component
  (let [entity (create-entity)
        pos {:type :position :x 5 :y 5}]
    (is (= (-> @system
               (add-entity entity)
               (add-component entity pos)
               (get-component entity :position)) pos))))

;; If an entity doesn't have a component, it should return nil
(deftest entity-without-component-nil
  (let [entity (create-entity)
        pos (->Position 5 5)]
    (is (nil? (-> @system
                  (add-entity entity)
                  (get-component entity Position))))

    (is (nil? (-> @system
                  (add-component entity pos)
                  (get-component entity Velocity))))))

;; Can retrieve all entites that have a single type
(deftest add-component-get-all-entities
  (is (= (get-all-entities-with-component @system Position)))

  (let [entity1 (create-entity)
        entity2 (create-entity)
        pos (->Position 5 5)]


    (-> @system
        (add-entity entity1)
        (add-entity entity2)
        (add-component entity1 pos)
        (add-component entity2 pos)
        r!)

    (is (= (frequencies (get-all-entities-with-component @system Position))
           (frequencies [entity1, entity2])))))

(deftest add-component-get-all-extended-entities
  (is (= (get-all-entities-with-component @system :position)))

  (let [entity1 (create-entity)
        entity2 (create-entity)
        pos {:type :position :x 5 :y 5}]

    (-> @system
        (add-entity entity1)
        (add-entity entity2)
        (add-component entity1 pos)
        (add-component entity2 pos)
        r!)

    (is (= (frequencies (get-all-entities-with-component @system :position))
           (frequencies [entity1, entity2])))))

;; Are able to removing an entity's component

(deftest remove-entity-component
  (let [entity (create-entity)
        pos (->Position 5 5)
        vel (->Velocity 10 10)]
    (-> @system
        (add-entity entity)
        (add-component entity pos)
        (add-component entity vel)
        r!)

    (is (get-component @system entity Position))
    (is (get-component @system entity Velocity))
    (is (= (get-all-entities-with-component @system Position) [entity]))
    (is (= (get-all-entities-with-component @system Velocity) [entity]))

    (-> @system (remove-component entity pos) r!)

    (is (nil? (get-component @system entity Position)))
    (is (get-component @system entity Velocity))
    (is (= (get-all-entities-with-component @system Position) []))
    (is (= (get-all-entities-with-component @system Velocity) [entity]))

    (-> @system (remove-component entity vel) r!)

    (is (nil? (get-component @system entity Position)))
    (is (nil? (get-component @system entity Velocity)))
    (is (empty? (get-all-entities-with-component @system Position)))
    (is (empty? (get-all-entities-with-component @system Velocity)))))

;; You can kill an entity, and it goes bye bye
(deftest kill-entity-goes-bye-bye
  (let [entity (create-entity)
        pos (->Position 5 5)
        vel (->Velocity 10 10)]

    (is (= (-> @system
               (add-entity entity)
               (add-component entity pos)
               (add-component entity vel)
               r!
               (get-all-entities)) [entity]))

    (-> @system
        (kill-entity entity)
        r!)

    (is (empty? (get-all-entities @system)))
    (is (nil? (get-component @system entity Position)))
    (is (nil? (get-component @system entity Velocity))))
  )

;; You can get all the components on a single entity, if you so choose
(deftest get-all-the-components-on-an-entity
  (let [entity (create-entity)
        pos (->Position 5 5)
        vel (->Velocity 10 10)]
    (is (empty? (-> @system
                    (add-entity entity)
                    r!
                    (get-all-components-on-entity entity))))
    (is (= (-> @system
               (add-component entity pos)
               r!
               (get-all-components-on-entity entity)) [pos]))

    (-> @system
        (add-component entity vel)
        r!)
    (is (= (frequencies (get-all-components-on-entity @system entity)) (frequencies [pos vel])))

    (is (empty?
          (-> @system
              (kill-entity entity)
              (get-all-components-on-entity entity))))))

;; You can update a component by applying a function and parameters to it, like update-in does
(deftest update-component-with-fn
  (let [entity (create-entity)
        pos (->Position 5 5)]
    (-> @system
        (add-entity entity)
        (add-component entity pos)
        r!)
    (is (= (:x (get-component @system entity Position)) 5))

    (-> @system
        (update-component entity Position assoc :x 10)
        r!)
    (is (= (:x (get-component @system entity Position)) 10))

    ;; send the same thing again, should be the same on the other side
    (-> @system
        (update-component entity Position assoc :x 10)
        r!)
    (is (= (:x (get-component @system entity Position)) 10))

    (-> @system
        (update-component entity Position (fn [_] nil))
        r!)

    (is (= (:x (get-component @system entity Position)) 10))))
