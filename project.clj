(defproject subway "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/java.jdbc "0.3.0-alpha1"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.clojure/data.json "0.2.2"]
                 [compojure "1.1.5"]]
  :plugins [[lein-ring "0.7.1"]]
  :ring {:handler subway.routes/app})
