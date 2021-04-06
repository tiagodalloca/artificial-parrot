(ns artificial-parrot.messenger.api.routes
  (:require [artificial-parrot.messenger.api.handlers :as api-handlers]
            [clojure.java.io :as io]
            [malli.util :as mu]
            [muuntaja.core :as m]
            reitit.coercion.malli
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            reitit.ring.malli
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]))

(def message-schema
  [:map [:text string?]])

(comment
  (malli.core/validate message-schema {:text "Hi!"}))

(defn routes []
  [["api/message"
    {:post {:parameters {:body message-schema}
            :responses {200 {:body [:map [:status string?]]}}
            :handler (fn [{{{:keys [text]} :body} :parameters}]
                       (api-handlers/message-post text))}}]])

(def options
  {;;:reitit.middleware/transform dev/print-request-diffs ;; pretty diffs
   ;;:validate spec/validate ;; enable spec validation for route data
   ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
   :exception pretty/exception
   :data
   {:coercion
    (reitit.coercion.malli/create
     {;; set of keys to include in error messages
      :error-keys
      #{:type :coercion :in :schema :value :errors :humanized :transformed}
      ;; schema identity function (default: close all map schemas)
      :compile mu/closed-schema
      ;; strip-extra-keys (effects only predefined transformers)
      :strip-extra-keys true
      ;; add/set default values
      :default-values true
      ;; malli options
      :options nil})
    :muuntaja m/instance
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}})
