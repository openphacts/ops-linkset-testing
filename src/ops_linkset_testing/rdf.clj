(ns ops-linkset-testing.rdf
  (:import
    [org.apache.jena.graph Node Triple]
    [org.apache.jena.riot RDFDataMgr]
    [org.apache.jena.riot.system StreamRDF])
  (:require
    [clojure.core.async :as async :refer [>!! <!! chan close!]]))

(defn- has-predicate? [predicate ^Triple triple]
  (.hasURI (.getPredicate triple) predicate))

(defn node-to-uristr [^Node node]
  (and (.isURI node) (.getURI node)))

(defn- triple->pair [triple]
  [ (node-to-uristr (.getSubject triple))
    (node-to-uristr (.getObject triple))
  ] )

(defn- channel-rdfstream [channel]
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

(defn linkset [url predicate]
  "Retrieve linkset from RDF url.
  Return channel that produces [src dst] links.
  Only the triples of the give predicate uri are included."
  (let [transducer (comp
          (filter (partial has-predicate? predicate))
          (map triple->pair))
        triples (async/chan 10 transducer)]
      (async/thread
        (println "Downloading linkset" url)
        (println "Filtering for predicate" predicate)
        (try
          (RDFDataMgr/parse (channel-rdfstream triples) url)
        (finally
          (async/close! triples))))
      triples))
