(ns brute.core-test
    (:import (java.util UUID))
    (:use [midje.sweet]
          [brute.core]))

(defn- setup!
    "Provides setup for the tests. Has side effects"
    []
    (reset-all!))

(namespace-state-changes (before :facts (setup!)))

(fact "The Entity I create is a unique uuid"
      (let [uuid (create-entity!)]
          uuid => truthy
          (> (-> uuid .toString .length) 0) => true
          (class uuid) => UUID
          (create-entity!) =not=> uuid))

(fact "Creating an entity results in it being added to the global list"
      (let [uuid (create-entity!)]
          [uuid] => (get-all-entities)))