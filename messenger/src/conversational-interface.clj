(ns conversational-interface)

(defprotocol MessengerInterfaceProtocol
  "MessengerInterfaceProtocol"
  (put-message [this message direction]
    "Puts a new `messege` in the queue to be delivered. `direction` may be :incoming or :outgoing")
  (deliver-messeges! [this]
    "Deliver messeges in the queue"))

(defrecord TerminalMessenger
    [queue]
  MessengerInterfaceProtocol
  (put-message [this message direction]
    (assoc this :queue (conj queue {:message message :direction direction})))
  (deliver-messeges! [this]
    (do (doseq [{:keys [message direction]} queue]
          (println (str (if-not (= direction :outgoing) ">" "") message))
          (assoc this :queue (clojure.lang.PersistentQueue/EMPTY))))))

(comment
  (-> (->TerminalMessenger (clojure.lang.PersistentQueue/EMPTY))
      (put-message "salve" :outgoing)
      (put-message "salve salve" :incoming)
      (put-message "nossa! funcionou :)" :incoming)
      (put-message (read-line) :outgoing)
      (put-message "tendi nada kk" :incoming)
      (deliver-messeges!)))

