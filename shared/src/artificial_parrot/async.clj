(ns artificial-parrot.async
  (:import [java.util.concurrent Executors]))

(def ^:dynamic *default-thread-pool* nil)

(defn create-thread-pool [pool-size]
  (Executors/newFixedThreadPool pool-size))

(defn execute-async
  ([pool f args]
   (.submit pool (fn [] (apply f args)))
   true)
  ([f args]
   (when *default-thread-pool* (execute-async *default-thread-pool* f args))))
