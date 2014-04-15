(defproject brute "0.2.0-SNAPSHOT"
    :description "A simple and lightweight Entity Component System library for writing games with Clojure"
    :url "http://www.github.com/markmandel/brute"
    :license {:name "Eclipse Public License"
              :url  "http://www.eclipse.org/legal/epl-v10.html"}
    :dependencies [[org.clojure/clojure "1.6.0"]]
    :plugins [[lein-midje "3.1.1"]
              [lein-ancient "0.5.5"]
              [codox "0.6.7"]]
    :profiles {:dev {:dependencies [[midje "1.6.3"]
                                    [org.clojure/tools.namespace "0.2.4"]
                                    [org.clojure/tools.trace "0.7.8"]
                                    [org.clojars.gjahad/debug-repl "0.3.3"]]
                     :source-paths ["dev"]
                     :repl-options {:init-ns user}
                     :codox        {:output-dir "doc/codox"}}})
