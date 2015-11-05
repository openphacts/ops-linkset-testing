(ns ops-linkset-testing.core
  (:require
    [ops-linkset-testing.ims :as ims]
    [ops-linkset-testing.rdf :as rdf]
    [ops-linkset-testing.ops :as ops]
    [ops-linkset-testing.utils :refer :all]
    [clj-http.client :as client]
    [clojure.core.async :as async]
    ;[cheshire.core :as json]
    )
  (:gen-class))


(defn ims-test [[src dst]]
  (and
    (contains? (ims/map-uri src) dst)
    (contains? (ims/map-uri dst) src)
  ))

(defn test-mapping-set-ims [ims mapping-set-url]
  (let [links (ims/sample-mapping-set (async/to-chan mapping-set-url))
        failed (async/chan 10 (filter #(not (ims-test ims %))))]
    (async/pipe links failed)
    (chan-seq!! failed 2000)))

(defn test-ims [ims ops]
  (binding [ims/**ims** ims
            ops/**ops** ops]
    (let [mappingsets (ims/all-mappingsets 300)
        errors (async/map #(async/to-chan (test-mapping-set-ims %)) mappingsets)]
        (async/map println errors))))

(defn -main
  [& args]
  (if (< (count args) 2) (println
"Usage: lein run [IMS] [OPS]
Example:
  lein run http://openphacts.cs.man.ac.uk:9095/QueryExpander/ https://beta.openphacts.org/1.5/")
    (test-ims (first args) (second args))))
