(ns ^{:doc "Core API for the Brute Entity Component System"}
    brute.core
    )

(def ^{:private true} all-entities (ref []))
(def ^{:private true} entity-components (ref {}))

(defn reset-all!
    "Resets the state of this entity component system. Good for tests"
    []
    (alter-var-root #'all-entities (constantly (ref [])))
    (alter-var-root #'entity-components (constantly (ref {}))))

(defn create-entity!
    "Creates an entity and stores it"
    []
    (let [entity (java.util.UUID/randomUUID)]
        (dosync
            (alter all-entities conj entity))
        entity))

(defn get-all-entities
    "Returns all the entities. Not that useful in application, but good for debugging/testing"
    []
    @all-entities)

(defmulti get-component-type
          "Returns the type for a given component. Using a multimethod with 'class' as the dispatch-fn to allow for extensibility per application.
          By default returns the class of the component."
          class)

(defmethod get-component-type :default
           [component]
    (class component))

(defn add-component!
    "Add a component instance to a given entity"
    [entity instance]
    (dosync
        (alter entity-components assoc-in [(get-component-type instance) entity] instance)))

(defn get-component
    "Get the component data for a specific component type"
    [entity type]
    (get-in @entity-components [type entity]))

(defn get-all-entities-with-component
    "Get all the entities that have a given component type"
    [type]
    (if-let [entities (keys (get @entity-components type))]
        entities
        []))

(defn remove-component!
    "Remove a component instance from an entity"
    [entity instance]
    (let [type (get-component-type instance)]
        (dosync (alter entity-components assoc type (dissoc (get @entity-components type) entity)))))

;; TODO: kill-entity
;; TODO: get-all-components-on-entity
;; TODO: process-one-game-tick
;; TODO: register-system
