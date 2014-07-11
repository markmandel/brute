(ns user
    "Tools for interactive development with the REPL. This file should
    not be included in a production build of the application."
    (:use [midje.repl :only (autotest load-facts)]
          [clojure.pprint :only (pprint)]
          [clojure.repl]
          [clojure.tools.namespace.repl :only (refresh refresh-all set-refresh-dirs)]
          [clojure.tools.trace])
    (:require [brute.entity :as em]))

;; system init functions
(def system
    "A Var containing an object representing the application under
      development."
    nil)

(defn create
    "Creates and initializes the system under development in the Var
      #'system."
    []
    (alter-var-root #'system (constantly (em/create-system))))

(defn go
    "Initializes and starts the system running."
    []
    (create)
    :ready)

(defn reset
    "Stops the system, optionally reloads modified source files, and restarts it."
    []
    (refresh :after 'user/go))

;; helper functions

(defn autotest-focus
    "Only autotest on the focused item"
    []
    (autotest :stop)
    (autotest :filter :focus))

(defn load-facts-focus
    "Only load tests under focus"
    []
    (load-facts :filter :focus))