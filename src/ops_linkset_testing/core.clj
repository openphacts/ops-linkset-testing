(ns ops-linkset-testing.core
  (:import
    [org.apache.jena.graph Node Triple]
    [org.apache.jena.riot RDFDataMgr]
    [org.apache.jena.riot.system StreamRDF])
  (:require
    [clj-http.client :as client]
    [clojure.core.async :as async]
    ;[cheshire.core :as json]
    )
  (:gen-class))

(def API_ID "161aeb7d")
(def API_KEY "ec09282901cef4df3eb40db86adb1b9f")


(defn url-to-json [url]
  (:body (client/get url {:accept :json :as :json})))

(defn mapping-set-info [url]
  (:MappingSetInfo (url-to-json url)))

(defn has-predicate? [predicate ^Triple triple]
  (.hasURI (.getPredicate triple) predicate))

(defn channel-rdfstream [channel]
  (proxy [StreamRDF] []
    (base [base] nil)
    (finish [] nil)
    (prefix [prefix iri] nil)
    (start [] nil)
    (triple [triple]
      ;(println triple)
      (async/>!! channel triple))
    (quad [quad]
      ;(println quad)
      (async/>!! channel quad))))

(defn <timeout
  "Like <!!, but with a timeout in milliseconds.

  Return nil if timed out or the channel is closed.
  Note that ch is not closed at timeout."
  [ch timeout]
  (first (async/alts!! [ch (async/timeout timeout)])))

(defn chan-seq!!
  "Convert a channel to a lazy sequence."
  { :author ["Timothy Baldridge", "amalloy", "Stian Soiland-Reyes"]
    :wasDerivedFrom "http://stackoverflow.com/a/26656917" }
  ([ch] (with-meta (lazy-seq
      (when-some [v (async/<!! ch)]
        (cons v (chan-seq!! ch)))) {:channel ch} ))
  ([ch timeout] (with-meta (lazy-seq
      (when-some [v (<timeout ch timeout)]
        (cons v (chan-seq!! ch timeout)))) {:channel ch :timeout timeout})))

(defn to-chan [coll]
  (or (:channel (meta coll))
      (async/to-chan coll)))

(defn sample
  "Return a transducer that randomly sample
  roughly n elements from roughly size elements.
  "
  ([n size]
    (random-sample (/ n size)))
  ([n size coll]
    (random-sample (/ n size) coll)))

(defn node-to-uristr [^Node node]
  (and (.isURI node) (.getURI node)))

(defn triple->pair [triple]
  [ (node-to-uristr (.getSubject triple))
    (node-to-uristr (.getObject triple))
  ] )

(defn linkset [url predicate]
  (println "Downloading linkset" url)
  (println "Filtering for predicate" predicate)
  (let [transducer (comp
          (filter (partial has-predicate? predicate))
          (map triple->pair))
        triples (async/chan 10 transducer)]
      (async/thread
        (try
          (RDFDataMgr/parse (channel-rdfstream triples) url)
        (finally
          (async/close! triples))))
      triples))

(defn mapping-set [url]
  (let [mapset (mapping-set-info url)
        links (linkset
                (:mappingSource mapset)
                (:predicate mapset))
        samples (async/chan 10 (sample 1000 (:numberOfLinks mapset)))]
      (async/pipe links samples)
      (chan-seq!! samples 2000)))

(defn map-uri [ims uri]
  (set (get-in (client/get (str ims "mapUri")
      { :query-params { "Uri" uri }
      :accept :json :as :json}))
      [:body :Mapping :targetUri]))

(defn ims-test [ims [src dst]]
  (and
    (contains? (map-uri ims src) dst)
    (contains? (map-uri ims dst) src)
  ))

(defn test-mapping-set-ims [ims mapping-set-url]
  (let [links (mapping-set (to-chan mapping-set-url))
        failed (async/chan 10 (filter #(not (ims-test ims %))))]
    (async/pipe links failed)
    (chan-seq!! failed 2000)))

(defn linkset-exists? [uri]
  (client/success?  (client/head uri {  :accept :json :throw-exceptions false })))

(defn all-mappingsets [ims max]
  (async/filter< linkset-exists?
    (async/to-chan (map (partial str ims "mappingSet/") (range max)))))

(defn test-ims [ims ops]
  (let [mappingsets (all-mappingsets ims 300)
        errors (async/map #(to-chan (test-mapping-set-ims %)) mappingsets)]
    (async/map println errors)))

(defn -main
  [& args]
  (if (< (count args) 2) (println
"Usage: ops-linkset-testing [IMS] [OPS]
Example
  ops-linkset-testing http://openphacts.cs.man.ac.uk:9095/QueryExpander/ https://beta.openphacts.org/1.5/")
    (test-ims (first args) (second args))))
