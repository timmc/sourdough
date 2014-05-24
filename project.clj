(defproject org.timmc/sourdough "0.1.0-SNAPSHOT"
  :description "Sourdough tracker and analyzer"
  :url "https://github.com/timmc/sourdough"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.4"]
                 [clj-time "0.6.0"]
                 [clj-jgit "0.6.4"]
                 [clojopts "0.3.2"]
                 [org.timmc/handy "1.6.0"]
                 [slingshot "0.10.3"]]
  :plugins [[org.timmc/lein-otf "2.0.1"]]
  :main ^:skip-aot org.timmc.sourdough.cli
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
