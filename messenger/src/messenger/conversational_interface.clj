(ns messenger.conversational-interface)

(defprotocol MessengerInterfaceProtocol
  "MessengerInterfaceProtocol"
  (put-message [this text direction]
    "Puts a new `messege` in the queue to be delivered. `direction` may be :incoming or :outgoing")
  (deliver-messeges! [this] [this sent-messages-atom]
    "Deliver messeges in the queue"))

(defrecord TerminalMessenger
    [queue]
  MessengerInterfaceProtocol
  (put-message [this text direction]
    (assoc this :queue (conj queue {:text text :direction direction})))
  (deliver-messeges! [this sent-messages-atom]
    (do (doseq [{:keys [text direction] :as message} queue]
          (when (= direction :incoming) (println (str ">" text)))
          (when sent-messages-atom
            (swap! sent-messages-atom
                   (fn [x] (conj x (assoc message :timestamp
                                         (str (java.time.Instant/now))))))))
        (assoc this :queue (clojure.lang.PersistentQueue/EMPTY))))
  (deliver-messeges! [this]
    (deliver-messeges! this nil)))

(comment
  (let [sent-messages-atom (atom [])]
    (-> (->TerminalMessenger (clojure.lang.PersistentQueue/EMPTY))
        (put-message "salve" :outgoing)
        (put-message "salve salve" :incoming)
        (put-message "nossa! funcionou :)" :incoming)
        (put-message "tendi nada kk" :incoming)
        (deliver-messeges! sent-messages-atom))
    @sent-messages-atom))

(defn terminal-messenger [] (->TerminalMessenger (clojure.lang.PersistentQueue/EMPTY)))
