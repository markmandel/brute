(ns brute.system-test
  "Tests for the system namespace"
  (:require [brute.entity :refer [create-system get-all-entities add-entity create-entity]]
            [brute.system :refer [add-system-fn
                                  add-throttled-system-fn
                                  process-one-game-tick]]
    #?(:clj
            [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest is use-fixtures]])))

(def system (atom 0))
(defrecord Position [x y])
(defrecord Velocity [x y])

(defn- setup!
  "Provides setup for the tests. Has side effects"
  [f]
  (reset! system (create-system))
  (f))

(defn- r! [s] (reset! system s))

(use-fixtures :each setup!)

;; You can add system functions, and then call them per game tick
(deftest add-system-function
  (let [counter (atom 0)
        sys-fn (fn [sys _] (swap! counter inc) sys)]

    (process-one-game-tick @system 10)
    (is (= @counter 0))

    (-> @system (add-system-fn sys-fn) r!)

    (process-one-game-tick @system 10)
    (is (= @counter 1))

    (-> @system (add-system-fn sys-fn) r!)

    (process-one-game-tick @system 10)
    (is (= @counter 3))))

;; Each system function will pass through the system ES data structure
(deftest pass-through-system
  (let [sys-fn (fn [system _] system)
        e (create-entity)]

    (is (= (-> @system
               (add-system-fn sys-fn)
               (add-entity e)
               (process-one-game-tick 0)
               (get-all-entities)) [e]))))

;; Calling a throttled function will only fire on every throttling call
(deftest throttled-function
  (let [counter (atom 0)
        threshold (/ 1000 60)
        sys-fn (fn [sys _] (swap! counter inc) sys)]

    (-> @system (add-throttled-system-fn sys-fn threshold) r!)

    (process-one-game-tick @system 0)
    (is (= @counter 0))

    (process-one-game-tick @system 10)
    (is (= @counter 0))

    (process-one-game-tick @system 7)
    (process-one-game-tick @system 7)
    (is (= @counter 1))

    (process-one-game-tick @system 0)
    (is (= @counter 1))

    (process-one-game-tick @system 35)
    (is (= @counter 3))))

;; Each throttled function will pass through the system ES data structure
(deftest throttled-fn-pass-through-system
  (let [counter (atom 0)
        threshold (/ 1000 60)
        sys-fn (fn [es _] (swap! counter inc) es)
        e (create-entity)]

    (-> @system
        (add-throttled-system-fn sys-fn threshold)
        (add-entity e)
        (process-one-game-tick 30)
        r!)

    (is (= @counter 1))
    (is (= (get-all-entities @system) [e]))))