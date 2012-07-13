(defproject jat "0.0.1"
  :url "https://github.com/technomancy/leiningen"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :description "Library for core functionality of Leiningen."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [congomongo/congomongo "0.1.7"]
                 [org.clojure/algo.monads "0.1.0"]]
  :repositories {
		"clojars" "http://cdldevstrbld02:9090/nexus/content/repositories/clojars/"
		"nexus" "http://cdldevstrbld02:9090/nexus/content/groups/public/"
		"maven" "http://cdldevstrbld02:9090/nexus/content/repositories/central/"
  }
  :omit-default-repositories true
  :profiles {:dev {:resource-paths ["dev-resources"]}
             :release {:aot :all}})
