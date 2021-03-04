(ns messenger.notifier.http
  (:require [clj-http.client :as http-client]))

(defn- prepare-body [message]
  message)

(defn http-notify! [url message]
  (http-client/post
   url
   {:body (prepare-body message)
    :accept :json}))

