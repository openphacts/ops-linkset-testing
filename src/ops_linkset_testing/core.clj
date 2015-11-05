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

(defn chan-seq!! [ch]
  "Convert a channel to a lazy sequence."
  { :author ["Timothy Baldridge", "amalloy", "Stian Soiland-Reyes"]
    :wasDerivedFrom "http://stackoverflow.com/a/26656917" }
  (lazy-seq
    (when-some [v (async/<!! ch)]
      (cons v (chan-seq!! ch)))))

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
        samples (async/chan 0)]
      (pipeline 1 links (sample 1000 (:numberOfLinks mapset)) samples)
      (chan-seq!! samples)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [transducer] (comp
      ()
      )
  (println "Hello, World!"))
