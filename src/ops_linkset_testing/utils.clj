(ns ops-linkset-testing.utils
  (:require
    [clojure.core.async :as async :refer [>!! <!! chan close!]]))

(defn <timeout
  "Like <!!, but with a timeout in milliseconds.

  Return nil if timed out or the channel is closed.
  Note that ch is not closed at timeout."
  [ch timeout]
  (first (async/alts!! [ch (async/timeout timeout)])))

(defn chan-seq!!
  "Convert a channel to a lazy sequence.

  Note: If a timeout is not provided, accessing the sequence
  is blocking, potentially forever."
  { :author ["Timothy Baldridge", "amalloy", "Stian Soiland-Reyes"]
    :wasDerivedFrom "http://stackoverflow.com/a/26656917" }
  ([ch] (lazy-seq
      (when-some [v (async/<!! ch)]
        (cons v (chan-seq!! ch)))))
  ([ch timeout] (lazy-seq
      (when-some [v (<timeout ch timeout)]
        (cons v (chan-seq!! ch timeout))))))

(defn sample
  "Randomly sample roughly n elements from coll of roughly size elements.
  If no collection is provided, return a transducer."
  ([n size]
    (random-sample (/ n size)))
  ([n size coll]
    (random-sample (/ n size) coll)))
