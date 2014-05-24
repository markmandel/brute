(ns brute.system-test
    (:use [midje.sweet]
          [brute.entity]
          [brute.system]))

(def system (atom 0))

(defn- setup!
    "Provides setup for the tests. Has side effects"
    []
    (reset! system (create-system)))

(defn- r! [s] (reset! system s))

(namespace-state-changes (before :facts (setup!)))

(defrecord Position [x y])
(defrecord Velocity [x y])

(fact "You can add system functions, and then call them per game tick"
      (let [counter (atom 0)
            sys-fn (fn [_ _] (swap! counter inc))]
          (process-one-game-tick @system 10)
          @counter => 0

          (-> @system
              (add-system-fn sys-fn)
              r!)

          (process-one-game-tick @system 10)
          @counter => 1

          (-> @system
              (add-system-fn sys-fn)
              r!)

          (process-one-game-tick @system 10)
          @counter => 3))

(fact "Each system function will pass through the system ES data structure" :focus
      (let [sys-fn (fn [system _] system)
            e (create-entity)]
          (-> @system
              (add-system-fn sys-fn)
              (add-entity e)
              (process-one-game-tick 0)
              (get-all-entities)) => [e]))

(fact "Calling a throttled function will only fire on every throttling call"
      (let [counter (atom 0)
            throttle-limit (atom 0)
            threshold (/ 1000 60)
            sys-fn (fn [_ _] (swap! counter inc))]

          (-> @system
              (add-throttled-system-fn sys-fn throttle-limit threshold)
              r!)

          (process-one-game-tick @system 0)
          @counter => 0

          (process-one-game-tick @system 10)
          @counter => 0

          (process-one-game-tick @system 7)
          @counter => 1

          (> @throttle-limit 0) => true

          (process-one-game-tick @system 0)
          @counter => 1

          (process-one-game-tick @system 35)
          @counter => 3
          ))