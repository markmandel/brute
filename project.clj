(defproject brute "0.2.1-SNAPSHOT"
            :description "A simple and lightweight Entity Component System library for writing games with Clojure"
            :url "http://www.github.com/markmandel/brute"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2268"]
                           [org.clojure/math.numeric-tower "0.0.4"]
                           [im.chit/purnam.test "0.4.3"]]
            :plugins [[lein-midje "3.1.1"]
                      [lein-ancient "0.5.5"]
                      [codox "0.6.7"]
                      [lein-cljsbuild "1.0.4-SNAPSHOT"]
                      [com.keminglabs/cljx "0.4.0"]]
            :hooks [cljx.hooks]
            :source-paths ["target/generated/src/clj"]
            :resource-paths ["target/generated/src/cljs"]
            :test-paths ["target/generated/test/clj"]
            :cljx {:builds [{:source-paths ["src/cljx"]
                             :output-path  "target/generated/src/clj"
                             :rules        :clj}
                            {:source-paths ["src/cljx"]
                             :output-path  "target/generated/src/cljs"
                             :rules        :cljs}
                            {:source-paths ["test/cljx"]
                             :output-path  "target/generated/test/clj"
                             :rules        :clj}
                            {:source-paths ["test/cljx"]
                             :output-path  "target/generated/test/cljs"
                             :rules        :cljs}]}
            :cljsbuild {:test-commands {"karma" ["karma" "start"
                                                 "--single-run"]}
                        :builds        [{:id           "dev"
                                         :source-paths ["target/generated/src/cljs"]
                                         :compiler     {:output-to     "target/brute.js"
                                                        :output-dir    "target/js-dev"
                                                        :optimizations :none
                                                        :pretty-print  true
                                                        :source-map    true}}
                                        {:id           "test"
                                         :source-paths ["target/generated/src/cljs"
                                                        "target/generated/test/cljs"]
                                         :compiler     {:output-to     "target/test.js"
                                                        :optimizations :whitespace
                                                        :pretty-print  true}}]}
            :aliases {"cljx"      ["with-profile" "cljx" "cljx"] ;; https://github.com/lynaghk/cljx/issues/31
                      "cleantest" ["do" "clean,"
                                   "cljx,"                  ;; have to do this twice, as without the generated clj, the user namespace can't load.
                                   "midje,"                 ;; cljx hook runs before midje
                                   "cljsbuild" "test" "karma"]} ;; run karma (will compile all cljs builds before, *sigh*)
            :profiles {:dev  {:dependencies [[midje "1.6.3"]
                                             [org.clojure/tools.namespace "0.2.4"]
                                             [org.clojure/tools.trace "0.7.8"]
                                             [org.clojars.gjahad/debug-repl "0.3.3"]]
                              :source-paths ["dev" "target/generated/src/clj"]
                              :repl-options {:init-ns user}
                              :codox        {:output-dir "doc/codox"}}
                       :cljx {}})
