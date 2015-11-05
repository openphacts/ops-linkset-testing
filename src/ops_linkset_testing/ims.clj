(ns ops-linkset-testing.ims
  (:require
    [ops-linkset-testing.rdf :as rdf]
    [ops-linkset-testing.utils :refer :all]
    [clj-http.client :as client]
    [clojure.core.async :as async]
    ;[cheshire.core :as json]
    ))

(def ^:dynamic **sample-size** 1000)
(def ^:dynamic **timeout-ms** 2000)
(def ^:dynamic **ims** "http://openphacts.cs.man.ac.uk:9095/QueryExpander/")


(defn url-to-json [url]
  (:body (client/get url {:accept :json :as :json})))

(defn mapping-set-info [url]
  (:MappingSetInfo (url-to-json url)))

(defn mappingset-exists? [uri]
  (client/success? (client/head uri { :accept :json :throw-exceptions false })))

(defn all-mappingsets [max]
  (async/filter< mappingset-exists?
    (async/to-chan (map #(str **ims** "mappingSet/" %) (range max)))))

(defn sample-mapping-set [url]
  (let [mapset (mapping-set-info url)
        links (rdf/linkset (:mappingSource mapset) (:predicate mapset))
        samples (async/chan 10
          (sample **sample-size** (:numberOfLinks mapset)))]
      (async/pipe links samples)
      (chan-seq!! samples **timeout-ms**)))

(defn map-uri [uri]
  (set (get-in
    (client/get (str **ims** "mapUri")
      { :query-params { "Uri" uri }
        :accept :json :as :json})
    [:body :Mapping :targetUri])))
