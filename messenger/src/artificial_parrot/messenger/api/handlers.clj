(ns artificial-parrot.messenger.api.handlers
  ;;(:require [messenger.events :refer [emitter] :rename {emitter events-emitter}])
  )

(defn message-post [{{{:keys [text]} :body} :parameters :as request}]
  ;; (eem/notify events-emitter
  ;;             :messenger.events/message-post
  ;;             {:text text :request request})
  ;; {:status 200 :body "ok"}
  )

