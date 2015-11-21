(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:use [clojure.repl]
        [clojure.tools.namespace.repl :only (refresh refresh-all set-refresh-dirs)])
  (:require [brute.entity :as es]
            [clojure.test :refer (run-tests run-all-tests)]))

;; system init functions
(def system
  "A Var containing an object representing the application under
    development."
  nil)

(defn create
  "Creates and initializes the system under development in the Var
    #'system."
  []
  (alter-var-root #'system (constantly (es/create-system))))

(defn go
  "Initializes and starts the system running."
  []
  (create)
  :ready)

(defn reset
  "Stops the system, optionally reloads modified source files, and restarts it."
  []
  (refresh :after 'user/go))