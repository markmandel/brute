(ns ^{:doc "Entity Manager functions for the Brute Entity Component System"}
    brute.entity)

(defn create-system
    "Creates the system data structure that will need to be passed to all entity functions"
    []
    {;; Nested Map of Component Types -> Entity -> Component Instance
        :entity-components      {}
     ;; Map of Entities -> Set of Component Types
        :entity-component-types {}})

(defn create-entity
    "Create the entity and return it. Entities are just UUIDs"
    []
    (java.util.UUID/randomUUID))

(defn get-all-entities
    "Returns a list of all the entities. Not that useful in application, but good for debugging/testing"
    [system]
    (if-let [result (-> system :entity-component-types keys)]
        result
        []))

(defn add-entity
    "Add the entity to the ES Data Structure and returns it"
    [system entity]
    (let [system (transient system)]
        (-> system
            (assoc! :entity-component-types (-> system :entity-component-types (assoc entity #{})))
            persistent!)))

(defmulti get-component-type
          "Returns the type for a given component. Using a multimethod with 'class' as the dispatch-fn to allow for extensibility per application.
          By default returns the class of the component."
          class)

(defmethod get-component-type :default
           [component]
    (class component))


(defn add-component
    "Add a component instance to a given entity in the ES data structure and returns it.
    Will overwrite a component if already set."
    [system entity instance]
    (let [type (get-component-type instance)
          system (transient system)
          ecs (:entity-components system)
          ects (:entity-component-types system)]
        (-> system
            (assoc! :entity-components (assoc-in ecs [type entity] instance))
            (assoc! :entity-component-types (assoc ects entity (-> ects (get entity) (conj type))))
            persistent!)))

(defn get-component
    "Get the component data for a specific component type"
    [system entity type]
    (-> system :entity-components (get-in [type entity])))

(defn update-component
    "Update an entity's component instance through through fn. Function is applied first with the specified component and any other args applied,
    and should return the modified component instance."
    [system entity type fn & args]
    (let [component (get-component system entity type)]
        (add-component system entity (apply fn component args))))

(defn get-all-entities-with-component
    "Get all the entities that have a given component type"
    [system type]
    (if-let [entities (-> system :entity-components (get type) keys)]
        entities
        []))

(defn remove-component
    "Remove a component instance from the ES data structure and returns it"
    [system entity instance]
    (let [type (get-component-type instance)
          system (transient system)
          entity-components (:entity-components system)
          entity-component-types (:entity-component-types system)]
        (-> system
            (assoc! :entity-components (assoc entity-components type (-> entity-components (get type) (dissoc entity))))
            (assoc! :entity-component-types (assoc entity-component-types entity (-> entity-component-types (get entity) (disj type))))
            persistent!)))

(defn kill-entity
    "Destroy an entity completely from the ES data structure and returns it"
    [system entity]
    (let [system (transient system)
          entity-component-types (:entity-component-types system)]
        (-> system
            (assoc! :entity-component-types (dissoc entity-component-types entity))
            (assoc! :entity-components (persistent! (reduce (fn [v type] (assoc! v type (dissoc (get v type) entity)))
                                                            (transient (:entity-components system)) (get entity-component-types entity))))
            persistent!)))

(defn get-all-components-on-entity
    "Get all the components on a specific entity. Useful for debugging"
    [system entity]
    (map #(get-in (:entity-components system) [% entity]) (get (:entity-component-types system) entity)))