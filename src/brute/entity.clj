(ns ^{:doc "Entity Manager functions for the Brute Entity Component System"}
    brute.entity)

(defn create-system
    "Creates the system data structure that will need to be passed to all entity functions"
    []
    ;; Set of all entities that are in the app
    {:all-entities           #{}
     ;; Map of Component Types -> Entity -> Component Instance
     :entity-components      {}
     ;; Map of Entities -> Set of Component Types
     :entity-component-types {}})

(defn create-entity
    "Create the entity and return it. Entities are just UUIDs"
    (java.util.UUID/randomUUID))

(defn get-all-entities
    "Returns all the entities. Not that useful in application, but good for debugging/testing"
    [system]
    (:all-entities system))

(defn store-entity
    "Store the entity in the Entity System"
    [system entity]
    (let [system (transient system)]
        (->
            system
            (assoc! :all-entities (conj! (get-all-entities) entity))
            (assoc! :entity-component-types (assoc! (:entity-component-types system) entity #{}))
            persistent!)))

#_
(defn create-entity!
    "Creates an entity and returns it. An entity is just an UUID"
    []
    (let (java.util.UUID/randomUUID)
        (dosync
            (alter all-entities conj entity)
            (alter entity-component-types assoc entity #{}))
        entity))

(defmulti get-component-type
          "Returns the type for a given component. Using a multimethod with 'class' as the dispatch-fn to allow for extensibility per application.
          By default returns the class of the component."
          class)

(defmethod get-component-type :default
           [component]
    (class component))

(defn add-component!
    "Add a component instance to a given entity. Will overwrite a component if already set."
    [entity instance]
    (dosync
        (let [type (get-component-type instance)]
            (alter entity-components assoc-in [type entity] instance)
            (alter entity-component-types assoc entity (conj (get @entity-component-types entity) type)))))

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
        (dosync
            (alter entity-components assoc type (dissoc (get @entity-components type) entity))
            (alter entity-component-types assoc entity (disj (get @entity-component-types entity) type)))))

(defn kill-entity!
    "Destroy an entity completely."
    [entity]
    (dosync
        (let [component-types (get @entity-component-types entity)]
            (alter all-entities disj entity)
            (alter entity-component-types dissoc entity)
            (doseq [type component-types]
                (alter entity-components assoc type (dissoc (get @entity-components type) entity))))))

(defn get-all-components-on-entity
    "Get all the components on a specific entity. Useful for debugging"
    [entity]
    (map #(get-in @entity-components [% entity]) (get @entity-component-types entity)))