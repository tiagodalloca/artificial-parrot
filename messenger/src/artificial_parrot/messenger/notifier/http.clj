(ns artificial-parrot.messenger.notifier.http
  (:require [artificial-parrot.messenger.notifier :refer [notify-message!]]
            [clj-http.client :as http-client]
            [clojure.data.json :as json]))

(defn- prepare-body [message]
  message)

(defn http-notify! [url message]
  (http-client/post
   url
   {:body (prepare-body message)
    :accept :json}))

(defmethod notify-message! ::http-notifier
  http-notify-message!
  [{:keys [url] :as opts} messages]
  (doseq [m messages]
    (http-notify! url m)))

