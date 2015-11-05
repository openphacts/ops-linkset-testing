(ns ops-linkset-testing.ops
  (:require
    [ops-linkset-testing.rdf :as rdf]
    [ops-linkset-testing.utils :refer :all]
    [clj-http.client :as client]
    [clojure.core.async :as async]
    ;[cheshire.core :as json]
    ))

(def ^:dynamic **api-id** "161aeb7d")
(def ^:dynamic **api-key** "ec09282901cef4df3eb40db86adb1b9f")
(def ^:dynamic **ops** "https://beta.openphacts.org/1.5/")
