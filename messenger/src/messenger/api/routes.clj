(ns messenger.api.routes
  (:require [reitit.ring :as ring]
            [reitit.coercion.malli]
            [reitit.ring.malli]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [muuntaja.core :as m]
            [clojure.java.io :as io]
            [malli.util :as mu]))

(def message-schema
  [:map [:text string?]])

(comment
  (malli.core/validate message-schema {:text "Hi!"}))

(defn routes []
  [["api/message"
    {:post {:parameters {:body message-schema}
            :responses {200 {:body [:map [:status string?]]}}
            :handler (fn [{{{:keys [text]} :body} :parameters}]
                       {:status 200 :body "ok"})}}]])


