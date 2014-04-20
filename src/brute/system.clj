(ns ^{:doc "Utility functions for system management. Systems in brute are simply functions that manage aspects like Physics, Rendering, Camera etc"}
    brute.system)

(defn add-system-fn
    "Add a function that represents a system, e.g. Physics, Rendering, etc.
    This needs to be in the structure: (fn [system delta]) where 'delta' is the number of milliseconds
    since the last game tick. This will also need to return the system in the state you want passed to the
    next system-fn, and ultimately out of process-one-game-tick.
    This will then be called directly when `process-one-game-tick` is called"
    [system system-fn]
    (assoc system :system-fns (conj (:system-fns system) system-fn)))

(defn process-one-game-tick
    "Optional convenience function that calls each of the system functions that have been added in turn, with the provided delta."
    [system delta]
    (reduce (fn [sys sys-fn] (sys-fn sys delta))
            system (:system-fns system)))