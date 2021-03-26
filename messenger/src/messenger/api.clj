(ns messenger.api
  (:require [messenger.api.routes :refer [routes]]
            [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]))

(defn app []
  (ring/ring-handler
   (ring/router (routes))))

(defn start-server []
  (with-local-vars [instance-app (app)]
    (jetty/run-jetty instance-app {:port 8080 :join? false})))

(comment
  (defonce server (start-server)))
