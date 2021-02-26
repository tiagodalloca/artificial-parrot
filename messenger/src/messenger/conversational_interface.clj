(ns messenger.conversational-interface)

(defprotocol MessengerInterfaceProtocol
  "MessengerInterfaceProtocol"
  (put-message [this text direction]
    "Puts a new `message` in the queue to be delivered. `direction` may be :incoming or :outgoing")
  (deliver-messages! [this] [this last-sent-message-atom]
    "Deliver messages in the queue"))

(defrecord TerminalMessenger
    [queue]
  MessengerInterfaceProtocol
  (put-message [this text direction]
    (assoc this :queue (conj queue {:text text :direction direction})))
  (deliver-messages! [this last-sent-message-atom]
    (do (doseq [{:keys [text direction] :as message} queue]
          (if (= direction :incoming)
            (println (str ">" text))
            (println text))
          (when last-sent-message-atom
            (reset! last-sent-message-atom
                    (assoc message :timestamp (str (java.time.Instant/now))))))
        (assoc this :queue (clojure.lang.PersistentQueue/EMPTY))))
  (deliver-messages! [this]
    (deliver-messages! this nil)))

(comment
  (let [last-sent-message-atom (atom nil)]
    (-> (->TerminalMessenger (clojure.lang.PersistentQueue/EMPTY))
        (put-message "salve" :outgoing)
        (put-message "salve salve" :incoming)
        (put-message "nossa! funcionou :)" :incoming)
        (put-message "tendi nada kk" :incoming)
        (deliver-messages! last-sent-message-atom))
    @last-sent-message-atom))

(defn terminal-messenger [] (->TerminalMessenger (clojure.lang.PersistentQueue/EMPTY)))

(defn messenger-put-message! [messenger-ref text direction]
  (swap! messenger-ref (fn [m] (put-message m text direction))))

(defn messenger-deliver! [messenger-ref last-sent-message-atom]
  (swap! messenger-ref (fn [m] (deliver-messages! m last-sent-message-atom))))
