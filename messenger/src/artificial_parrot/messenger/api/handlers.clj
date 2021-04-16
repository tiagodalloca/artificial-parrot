(ns artificial-parrot.messenger.api.handlers
  (:require [artificial-parrot.events :as events]
            [artificial-parrot.messenger.api :as api]))

(defn message-post [{:keys [emitter] :as deps}
                    {{{:keys [text]} :body} :parameters :as request}]
  (do
    (events/dispatch-event emitter ::api/message-post {:text text :request request})
    {:body {:message "message received; to be sent."}}))

(defn webhook-listeners-post [{:keys [emitter] :as deps}
                              {{{:keys [url headers queryParameters body]} :body} :parameters :as request}]
  (do
    (events/dispatch-event emitter ::api/webhook-listener-post
                           {:url url
                            :headers headers
                            :query-param queryParameters
                            :body body})
    {:body {:message "webhook listener posted; to be added."}}))

(defn get-handlers [deps]
  {::message-post (partial message-post deps)}
  {::webhook-listener-post (partial webhook-listeners-post deps)})
