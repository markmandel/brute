(defproject brute "0.4.0-SNAPSHOT"
  :description "A simple and lightweight Entity Component System library for writing games with Clojure"
  :url "http://www.github.com/markmandel/brute"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :plugins [[lein-codox "0.9.0"]
            [lein-cljsbuild "1.1.1"]]
  :cljsbuild {:builds
              [{:id           "src"
                :source-paths ["src"]
                :compiler     {:output-to     "target/js/src/brute.js"
                               :output-dir    "target/js/src"
                               :source-map    true
                               :optimizations :none
                               :pretty-print  true}}
               {:id           "test"
                :source-paths ["src" "test"]
                :compiler     {:output-to     "target/js/test/brute.js"
                               :output-dir    "target/js/test"
                               :source-map    true
                               :optimizations :none
                               :pretty-print  true}}]}
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]]
                   :source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :codox        {:output-dir "doc/codox"}}})