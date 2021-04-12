(ns artificial-parrot.messenger.api.handlers
  (:require [artificial-parrot.messenger.events :as events]
            [artificial-parrot.messenger.api :as api]))

(defn message-post [{:keys [emitter] :as deps}
                    {{{:keys [text]} :body} :parameters :as request}]
  (do
    ;; (events/add-observer emitter ::api/message-post ::message-post (fn [m] (println m)))
    (events/dispatch-event emitter ::api/message-post {:text text :request request})
    {:message "message received; to be sent."}))

(defn get-handlers [deps]
  {::message-post (partial message-post deps)})
