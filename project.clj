(defproject ops-linkset-testing "0.1.0-SNAPSHOT"
  :description "Testing linksets in Open PHACTS"
  :url "https://github.com/openphacts/ops-linkset-testing"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]
                 [clj-http "2.0.0"]

                ]
  :main ^:skip-aot ops-linkset-testing.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}

)
