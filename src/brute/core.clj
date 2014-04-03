(ns ^{:doc "Core API for the Brute Entity Component System"}
    brute.core)

(def ^{:private true} all-entities (atom []))

(defn reset-all!
    "Resets the state of this entity component system. Good for tests"
    []
    (alter-var-root #'all-entities (constantly (atom []))))

(defn create-entity!
    "Creates an entity and stores it"
    []
    (let [uuid (java.util.UUID/randomUUID)]
        (swap! all-entities conj uuid)
        uuid))

(defn get-all-entities
    "Returns all the entities. Not that useful in application, but good for debugging/testing"
    []
    @all-entities)