(ns brute.entity
  "Entity Manager functions for the Brute Entity Component System")

(defn create-system
  "Creates the system data structure that will need to be passed to all entity functions"
  []
  {;; Nested Map of Component Types -> Entity -> Component Instance
   :entity-components      {}
   ;; Map of Entities -> Set of Component Types
   :entity-component-types {}})

(defn create-uuid
  "create a UUID"
  []
  #?(:clj  (java.util.UUID/randomUUID)
     :cljs (let [template "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
                 f #(let [r (Math/floor (* (rand) 16))
                          v (if (= % \x) r (bit-or (bit-and r 0x3) 0x8))]
                     (.toString v 16))]
             (.replace template (js/RegExp. "[xy]" "g") f))))

(defn create-entity
  "Create the entity and return it. Entities are just UUIDs"
  []
  (create-uuid))

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
          #?(:clj  class
             :cljs type))

(defmethod get-component-type :default
  [component]
  (#?(:clj  class
      :cljs type) component))

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
  and should return the modified component instance. Return nil if you want no change to occur."
  [system entity type fn & args]
  (if-let [update (apply fn (get-component system entity type) args)]
    (add-component system entity update)
    system))

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
