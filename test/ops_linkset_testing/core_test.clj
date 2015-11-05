(ns ops-linkset-testing.core-test
  (:require [clojure.test :refer :all]
            [ops-linkset-testing.core :refer :all]))

(def MAPPINGSET "http://openphacts.cs.man.ac.uk:9095/QueryExpander/mappingSet/1")

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
      (is (< 2000 (count set1)))
      (println (first set1)))))
