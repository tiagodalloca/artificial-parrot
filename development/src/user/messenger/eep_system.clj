;; (ns user.messenger.eep-system
;;   (:require [artificial-parrot.messenger.conversational-interface
;;              :as
;;              messenger-interface
;;              :refer
;;              [messenger-deliver!
;;               messenger-put-message!
;;               put-message
;;               terminal-messenger]]
;;             [artificial-parrot.messenger.notifier :refer [notify-message!]]
;;             [clojurewerkz.eep.emitter :as eem]
;;             [integrant.core :as ig]
;;             [messenger.events :as events :refer [emitter]]
;;             [ring.mock.request :as ring-mock]))

;; (def config
;;   {::terminal-messenger
;;    {:introduction ["Let's shatter those dreams"]}
;;    ::emitter emitter
;;    ::println-notifier {:opts {:type messenger.notifier.stdout/stdout-notifier}
;;                        :emitter (ig/ref ::emitter)}})

;; (defonce terminal-messenger-ref (atom nil))

;; (defn send-message! [text]
;;   (when @terminal-messenger-ref
;;     (messenger-put-message! terminal-messenger text :outgoing)))

;; (defn deliver-messages! []
;;   (when @terminal-messenger-ref
;;     (let [last-message-ref (atom nil)]
;;       (add-watch last-message-ref
;;                  ::deliver-messages!
;;                  (fn [_ _ _ ns]
;;                    (eem/notify emitter :messenger-interface/delivered ns)))
;;       (messenger-deliver! m last-message-ref))))


;; (defmethod ig/init-key ::terminal-messenger [_ {:keys [introduction]}]
;;   (do (->> (reduce (fn [acc x] (put-message acc x :incoming))
;;                    (terminal-messenger (str "conversation/" (java.time.Instant/now)))
;;                    introduction)
;;            (reset! terminal-messenger-ref))
;;       terminal-messenger-ref))

;; (defmethod ig/init-key ::println-notifier [k {:keys [emitter opts]}]
;;   (eem/defobserver emitter k
;;     (fn [message] (notify-message! opts message))))

