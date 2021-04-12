(ns artificial-parrot.messenger.api
  (:require [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]))

(defn app [router]
  (ring/ring-handler router))

(defn start-server [{:keys [router port] :as deps}]
  (let [instance-app (app router)]
    (def instance-app instance-app)
    (jetty/run-jetty #'instance-app {:port port :join? false})))

