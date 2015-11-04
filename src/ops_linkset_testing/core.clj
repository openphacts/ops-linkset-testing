(ns ops-linkset-testing.core
  (:import org.apache.jena.riot RDFDataMgr)
  (:import org.apache.jena.riot.system StreamRDF)
  (:require
    [clj-http.client :as client]
    [clojure.core.async :as async :refer :all]
    ;[cheshire.core :as json]
    )
  (:gen-class))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn url-to-json [url]
  (:body (client/get url {:accept :json :as :json})))
;  (with-open [rdr (clojure.java.io/reader url)])
;  (json/parse-stream ))-

(defn mapping-set-info [url]
  (:MappingSetInfo (url-to-json url)))

(defn has-predicate? [predicate triple]
  (.hasURI predicate (.getPredicate triple)))

(defn channel-rdfstream [channel]
  (proxy StreamRDF
    (base [base])
    (finish [])
    (prefix [prefix iri])
    (quad [quad]
      (>!! channel qued))
    (start [])
    (triple [triple]
      (>!! channel triple))
  ))

(defn linkset [url predicate]
  (println url)
  (println predicate)

  (let [transducer (filter (partial has-predicate? predicate))
        triples (chan 10 transducer)]
    (thread (RDFDataMgr/parse url (channel-rdfstream triples)))
    triples)
    )

(defn mapping-set [url]
  (let [mapset (mapping-set-info url)]
    (linkset
      (:mappingSource mapset)
      (:predicate mapset)
  )))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
