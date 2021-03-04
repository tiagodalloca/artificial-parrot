(ns user.messenger.println-listener
  (:require [integrant.core :as ig]
            [messenger.conversational-interface :refer [terminal-messenger put-message messenger-put-message! messenger-deliver!]]))

(def config
  {:messenger/listeners {}
   
   :messenger/println-listener
   {:listeners-ref (ig/ref :messenger/listeners)}
   
   :messenger/terminal-messenger
   {:introduction ["Let's shatter those dreams"]}

   :messenger/last-sent-message {:listeners-ref (ig/ref :messenger/listeners)}})

(defn send-message! [text]
  (when-let [m (some-> integrant.repl.state/system (:messenger/terminal-messenger))]
    (messenger-put-message! m text :outgoing)))

(defn deliver-messages! []
  (when-let [m (some-> integrant.repl.state/system (:messenger/terminal-messenger))]
    (when-let [last-sent-message (some-> integrant.repl.state/system (:messenger/last-sent-message))]
      (messenger-deliver! m last-sent-message))))

(defmethod ig/init-key :messenger/listeners [_ _]
  (atom []))

(defmethod ig/init-key :messenger/terminal-messenger [_ {:keys [introduction]}]
  (do (->> (reduce (fn [acc x] (put-message acc x :incoming))
                   (terminal-messenger) introduction)
           (atom))))

(defmethod ig/init-key :messenger/println-listener [k {:keys [listeners-ref]}]
  (letfn [(println-watcher [_ _ _ message]
            (when message
              (println (str "println-listener>" message))))]
    (swap! listeners-ref (fn [coll] (conj coll [k println-watcher])))))

(defmethod ig/init-key :messenger/last-sent-message [_ {:keys [listeners-ref]}]
  (let [last-sent-message-atom (atom nil)]
    (doseq [[k f] @listeners-ref]
      (add-watch last-sent-message-atom k f))
    (add-watch listeners-ref :messenger/last-sent-message
               (fn [_ _ os ns]
                 (when (< (count os) (count ns))
                   (let [[k f] (last ns)]
                     (add-watch last-sent-message-atom k f)))))
    last-sent-message-atom))



