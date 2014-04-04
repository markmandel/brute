(ns ^{:doc "Utility functions for system management. Systems in brute are simply functions that manage aspects like Physics, Rendering, Camera etc"}
    brute.system)

;; seq of functions that relate to systems
(def ^{:private true} system-fns (atom []))

(defn reset-all!
    "Resets the state of this entity component system. Good for tests"
    []
    (alter-var-root #'system-fns (constantly (atom []))))

(defn add-system-fn
    "Add a function that represents a system, e.g. Physics, Rendering, etc.
    This needs to be in the structure: (fn [delta]) where 'delta' is the number of milliseconds since the last game tick.
    This will then be called directly when `process-one-game-tick` is called"
    [system-fn]
    (swap! system-fns conj system-fn))

(defn process-one-game-tick
    "Optional convenience function that calls each of the system functions that have been added in turn, with the provided delta."
    [delta]
    (doseq [system-fn @system-fns]
        (apply system-fn [delta])))
