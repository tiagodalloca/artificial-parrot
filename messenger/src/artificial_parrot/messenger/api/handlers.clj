(ns artificial-parrot.messenger.api.handlers
  (:require [artificial-parrot.events :as events]
            [artificial-parrot.messenger.api :as api]
            [artificial-parrot.utils :refer [md5]]))

(defn message-post [{:keys [emitter] :as deps}
                    {{{:keys [text]} :body} :parameters :as request}]
  (events/dispatch-event emitter ::api/message-post {:text text :request request})
  {:body {:message "message received; to be sent."}})

(defn webhook-listener-post [{:keys [emitter] :as deps}
                             {{{:keys [url headers queryParameters body]} :body} :parameters :as request}]
  (let [webhook-listener {:id (md5 url)
                          :url url
                          :headers headers
                          :query-param queryParameters
                          :body body}]
    (events/dispatch-event emitter ::api/webhook-listener-post webhook-listener)
    {:body {:message "webhook listener posted; to be added."
            :webhook-listener webhook-listener}}))

(defn webhook-listener-delete [{:keys [emitter] :as deps}
                               {{{:keys [id]} :query} :parameters :as request}]
  (events/dispatch-event emitter ::api/webhook-listener-delete id)
  {:body {:message (str "webhook listener of id=" id "to be deleted.")}})

(defn get-handlers [deps]
  {::message-post (partial message-post deps)}
  {::webhook-listener-post (partial webhook-listener-post deps)}
  {::webhook-listener-delete (partial webhook-listener-delete deps)})
