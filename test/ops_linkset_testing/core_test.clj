(ns ops-linkset-testing.core-test
  (:require [clojure.test :refer :all]
            [ops-linkset-testing.core :refer :all]))

(def IMS "http://openphacts.cs.man.ac.uk:9095/QueryExpander/")

(def MAPPINGSET (str IMS "/mappingSet/1"))

(def LINK ["http://bio2rdf.org/drugbank:BE0001112" "http://purl.uniprot.org/uniprot/P17213"])

(deftest mappingset
  (testing "url-to-json"
    (println "Retrieving " MAPPINGSET)
    (let [set1 (url-to-json MAPPINGSET)]
      (println set1)
      (is (= 1 (get-in set1 [:MappingSetInfo :id])))))
  (testing "mapping-set-info"
      (is (= 1 (:id (mapping-set-info MAPPINGSET)))))

  (testing "mapping-set"
    (let [set1 (mapping-set MAPPINGSET)]
      (is (< 100 (count set1)))
      (is (< (count set1) 2000))
      (println (first set1)))))

(deftest imscheck
  (testing "ims-test"
    (ims-test IMS LINK)
  ))
