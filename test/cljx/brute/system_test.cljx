(ns brute.system-test
    (:require #+clj [midje.sweet :refer :all]
              #+cljs [purnam.test] 
              [brute.entity :refer #+clj :all
               ;;         v--- wish cljs knew what to do with :refer :all 
               #+cljs [create-entity
                       create-system
                       add-entity
                       get-all-entities]]
              [brute.system :refer #+clj :all
               #+cljs [add-system-fn
                       add-throttled-system-fn
                       process-one-game-tick]])
    #+cljs (:require-macros [purnam.test :refer [fact]]))

#+cljs (declare =>)

(def system (atom 0))

(defn- setup!
    "Provides setup for the tests. Has side effects"
    []
    (reset! system (create-system)))

(defn- r! [s] (reset! system s))

#+clj (namespace-state-changes (before :facts (setup!)))
#+cljs (setup!)

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

(fact "Each system function will pass through the system ES data structure"
      (let [sys-fn (fn [system _] system)
            e (create-entity)]
          (-> @system
              (add-system-fn sys-fn)
              (add-entity e)
              (process-one-game-tick 0)
              (get-all-entities)) => [e]))

(fact "Calling a throttled function will only fire on every throttling call" :focus
      (let [counter (atom 0)
            threshold (/ 1000 60)
            sys-fn (fn [_ _] (swap! counter inc))]

          (-> @system
              (add-throttled-system-fn sys-fn threshold)
              r!)

          (process-one-game-tick @system 0)
          @counter => 0

          (process-one-game-tick @system 10)
          @counter => 0

          (process-one-game-tick @system 7)
          @counter => 1

          (process-one-game-tick @system 0)
          @counter => 1

          (process-one-game-tick @system 35)
          @counter => 3))

(fact "Each throttled function will pass through the system ES data structure"
      (let [counter (atom 0)
            threshold (/ 1000 60)
            sys-fn (fn [es _] (swap! counter inc) es)
            e (create-entity)]

          (-> @system
              (add-throttled-system-fn sys-fn threshold)
              (add-entity e)
              (process-one-game-tick 30)
              r!)
          @counter => 1
          (get-all-entities @system) => [e]))
