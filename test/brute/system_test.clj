(ns brute.system-test
    (:use [midje.sweet]
          [brute.system]))

(defn- setup!
    "Provides setup for the tests. Has side effects"
    []
    (reset-all!))

(namespace-state-changes (before :facts (setup!)))

(defrecord Position [x y])
(defrecord Velocity [x y])

(fact "You can add system functions, and then call them per game tick"
      (let [counter (atom 0)
            sys-fn (fn [delta] (swap! counter inc))]
          (process-one-game-tick 10)
          @counter => 0
          (add-system-fn sys-fn)
          (process-one-game-tick 10)
          @counter => 1
          (add-system-fn sys-fn)
          (process-one-game-tick 10)
          @counter => 3))

;; TODO: be able to register a system-fn with a throttle (i.e. only fire every 10 ms)