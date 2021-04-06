(ns artificial-parrot.messenger.events
  (:require [clojure.core.async :as async])
  (:import [java.util.concurrent Executors]))

(defn- execute-async [pool f args]
  (.submit pool (fn [] (apply f args))))

(defn- handle-dispach-async [{:keys [observers pool] :as emitter} [event-t & args]]
  (let [event-obs (get @observers event-t)]
    (when event-obs
      (doseq [[id f] event-obs]
        (execute-async pool f (vector args))))))

(defn start-listening [{:keys [observers chan exit-chan running?] :as emitter}]
  (async/go-loop []
    (async/alt!
      chan ([event]
            (handle-dispach-async emitter event)
            (recur))
      exit-chan (reset! running? false)))
  (reset! (:running? emitter) true))

(defn create-emitter
  ([{:keys [pool-size chan-buf-size chan-buf immediately-start?] :as opts}]
   (let [pool-size (or pool-size 8)
         chan (async/chan (or chan-buf chan-buf-size 8))
         emitter-ret {:observers (atom {})
                      :chan chan
                      :exit-chan (async/chan)
                      :pool (Executors/newFixedThreadPool 2)
                      :running? (atom false)}]
     (if immediately-start?
       (start-listening emitter-ret)
       emitter-ret)))
  ([] (create-emitter {})))

(defn stop-listening [{:keys [exit-chan] :as emitter}]
  (async/put! exit-chan true))

(defn add-observer [emitter event-t observer-id observer]
  (update
   emitter :observers
   (fn [obs-atom]
     (swap! obs-atom
            (fn [obs]
              (update obs event-t
                      (fn [m] (if m
                                (assoc m observer-id observer)
                                (array-map observer-id observer)))))))))

(defn remove-observer [emitter event-t observer-id]
  (update
   emitter :observers
   (fn [obs-atom]
     (swap! obs-atom
            (fn [obs]
              (update obs event-t
                      (fn [m] (when m (dissoc m observer-id)))))))))


(defn dispatch-event [{:keys [chan] :as emitter} event-t & args]
  (async/put! chan (into (vector event-t) args)))

(comment
  (let [test-emitter (emitter {:pool-size 1
                               :chan-buf-size 10
                               :immediately-start? true})]
    (add-observer emitter :oi :println-obs println)
    (dispatch-event test-emitter :oi)))

(comment
  (def test-emitter (create-emitter {:pool-size 1
                                     :chan-buf-size 10
                                     :immediately-start? false}))
  (start-listening test-emitter)
  (add-observer test-emitter :oi :println-obs println)
  (dispatch-event test-emitter :oi "oi")
  (remove-observer test-emitter :oi :println-obs)
  (stop-listening test-emitter)
  test-emitter)

