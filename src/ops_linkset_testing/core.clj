(ns ops-linkset-testing.core
  (:require
    [clj-http.client :as client]
    [cheshire.core :as json])
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

(defn linkset [url predicate]
  (println url)
  (println predicate)
  ;; TODO: Do for real
  [ "http://example.com/"]
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
