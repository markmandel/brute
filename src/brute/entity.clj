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
    []
    (java.util.UUID/randomUUID))

(defn get-all-entities
    "Returns all the entities. Not that useful in application, but good for debugging/testing"
    [system]
    (:all-entities system))

(defn add-entity
    "Add the entity in the Entity System"
    [system entity]
    (let [system (transient system)]
        (-> system
            (assoc! :all-entities (conj (get-all-entities system) entity))
            (assoc! :entity-component-types (-> system :entity-component-types (assoc entity #{})))
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


(defn add-component
    "Add a component instance to a given entity. Will overwrite a component if already set."
    [system entity instance]
    (let [type (get-component-type instance)
          system (transient system)
          ecs (:entity-components system)
          ects (:entity-component-types system)]
        (-> system
            (assoc! :entity-components (assoc-in ecs [type entity] instance))
            (assoc! :entity-component-types (assoc ects entity (-> ects (get entity) (conj type))))
            persistent!)))

#_
(defn add-component!
    "Add a component instance to a given entity. Will overwrite a component if already set."
    [entity instance]
    (dosync
        (let [type (get-component-type instance)]
            (alter entity-components assoc-in [type entity] instance)
            (alter entity-component-types assoc entity (conj (get @entity-component-types entity) type)))))

(defn get-component
    "Get the component data for a specific component type"
    [system entity type]
    (-> system :entity-components (get-in [type entity])))

#_
(defn get-component
    "Get the component data for a specific component type"
    [entity type]
    (get-in @entity-components [type entity]))

(defn get-all-entities-with-component
    "Get all the entities that have a given component type"
    [system type]
    (if-let [entities (-> system :entity-components (get type) keys)]
        entities
        []))

(defn remove-component
    "Remove a component instance from an entity"
    [system entity instance]
    (let [type (get-component-type instance)
          system (transient system)
          entity-components (:entity-components system)
          entity-component-types (:entity-component-types system)]
        (-> system
            (assoc! :entity-components (assoc entity-components type (-> entity-components (get type) (dissoc entity))))
            (assoc! :entity-component-types (assoc entity-component-types entity (-> entity-component-types (get entity) (disj type))))
            persistent!)))

#_
(defn remove-component!
    "Remove a component instance from an entity"
    [entity instance]
    (let [type (get-component-type instance)]
        (dosync
            (alter entity-components assoc type (dissoc (get @entity-components type) entity))
            (alter entity-component-types assoc entity (disj (get @entity-component-types entity) type)))))

#_
(reduce (fn [v e]
            (assoc v e (dissoc (get v e) :e)))
        m k)

(defn kill-entity
    "Destroy an entity completely."
    [system entity]
    (let [system (transient system)
          entity-component-types (:entity-component-types system)]
        (-> system
            (assoc! :all-entities (disj! (get-all-entities system)))
            (assoc! :entity-component-types (dissoc! entity-component-types entity))
            (assoc! :entity-components (reduce (fn [v type] (assoc! v type (dissoc! (get v type) entity)))
                                               (:entity-components system) (get entity-component-types entity)))
            persistent!)
        )
    )

#_
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
    [system entity]
    (map #(get-in (:entity-components system) [% entity]) (get (:entity-component-types system) entity)))