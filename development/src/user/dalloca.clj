(ns user.dalloca
  (:require [integrant.core :as ig]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [messenger.conversational-interface :refer [terminal-messenger put-message deliver-messeges!]]))

(def config
  {::messenger {:introduction ["Let's shatter those dreams"]}
   ::listener {:messenger-ref (ig/ref ::messenger)}})

(comment (integrant.repl/set-prep! (constantly config)))

(defmethod ig/init-key ::messenger [_ {:keys [introduction]}]
  (-> (reduce (fn [acc x] (put-message acc x :incoming))
              (terminal-messenger) introduction)
      (atom)))

(defmethod ig/init-key ::listener [_ {:keys [messenger-ref]}]
  (defn send-message! [text]
    (swap! messenger-ref (fn [m] (put-message m text :outgoing))))
  (defn messenger-deliver! []
    (swap! messenger-ref (fn [m] (deliver-messeges! m))))
  nil)
