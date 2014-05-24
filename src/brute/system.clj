(ns brute.system
    "Utility functions for system management.
    Systems in brute are simply functions that manage aspects like Physics, Rendering, Camera etc"
    (:require [clojure.math.numeric-tower :as m]))

(defn add-system-fn
    "Add a function that represents a system, e.g. Physics, Rendering, etc.
    This needs to be in the structure: (fn [system delta]) where 'delta' is the number of milliseconds
    since the last game tick. This will also need to return the system in the state you want passed to the
    next system-fn, and ultimately out of process-one-game-tick.
    This will then be called directly when `process-one-game-tick` is called"
    [system system-fn]
    (assoc system :system-fns (conj (:system-fns system) system-fn)))

(defn- throttled-fn
    "The function that does the actual throttling. "
    [system-fn atom threshhold system delta]
    (swap! atom + delta)
    (if (>= @atom threshhold)
        (reduce (fn [v _]                                   ;; this takes care of when the framerate
                    (swap! atom - threshhold)               ;; is WAY slower than the throttle.
                    (system-fn v delta))
                system (-> @atom (/ threshhold) m/floor range))
        system))

(defn add-throttled-system-fn
    "Same as `add-system-fn`, but will only execute the function after `threshold` milliseconds has been equalled or passed. This time
    that has been passed is stored in the `atom` that is passed in, which should have an initial value of 0 (and will be changed on each function call)."
    [system system-fn atom threshold]
    (add-system-fn system (partial throttled-fn system-fn atom threshold)))

(defn process-one-game-tick
    "Optional convenience function that calls each of the system functions that have been added in turn, with the provided delta."
    [system delta]
    (reduce (fn [sys sys-fn] (sys-fn sys delta))
            system (:system-fns system)))