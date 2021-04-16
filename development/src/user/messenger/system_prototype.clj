(ns user.messenger.system-prototype
  (:require [artificial-parrot.messenger.conversational-interface
             :as
             messenger-interface
             :refer
             [messenger-deliver!
              messenger-put-message!
              put-message
              terminal-messenger]]
            [artificial-parrot.messenger.notifier :refer [notify-message!]]
            [artificial-parrot.messenger.notifier.stdout :as stdout-notifier]
            [artificial-parrot.messenger.notifier.http :as http-notifier]
            [artificial-parrot.messenger.api.handlers :refer [get-handlers]]
            [artificial-parrot.messenger.api.router :refer [get-router]]
            [artificial-parrot.messenger.api :refer [start-server] :as api]
            [artificial-parrot.async :refer [execute-async]]

            [integrant.core :as ig]
            [integrant.repl :refer [clear go halt init prep reset reset-all]]
            [artificial-parrot.events :as events :refer [create-emitter]]
            [ring.mock.request :as ring-mock]))

(def config
  {::thread-pool {:pool-size 8}
   ::emitter  {:opts {:pool (ig/ref ::thread-pool)
                      :chan-buf-size 10
                      :immediately-start? true}}

   ::terminal-messenger {:introduction ["Let's shatter those dreams"]
                         :emitter (ig/ref ::emitter)}

   ::println-notifier {:opts {:type ::stdout-notifier/stdout-notifier}
                       :emitter (ig/ref ::emitter)}
   ::http-listeners {:emitter (ig/ref ::emitter)}
   ::http-notifier {:opts {:type ::http-notifier/http-notifier}
                    :http-listeners (ig/ref ::http-listeners)
                    :emitter (ig/ref ::emitter)}
   
   ::server {:router (ig/ref ::router)
             :port 6942}
   ::router {:handlers (ig/ref ::handlers)}
   ::handlers {:emitter (ig/ref ::emitter)}})

(integrant.repl/set-prep! (constantly config))

(defonce terminal-messenger-atom (atom nil))

(defonce last-message-atom (atom nil))

(defn send-message! [text]
  (when @terminal-messenger-atom
    (messenger-put-message! terminal-messenger-atom text :outgoing)))

(defn deliver-messages! []
  (when @terminal-messenger-atom
    (messenger-deliver! terminal-messenger-atom last-message-atom)))

(defmethod ig/init-key ::terminal-messenger [_ {:keys [introduction emitter]}]
  (do (->> (reduce (fn [acc x] (put-message acc x :incoming))
                   (terminal-messenger (str "conversation/" (java.time.Instant/now)))
                   introduction)
           (reset! terminal-messenger-atom))
      (add-watch last-message-atom
                 ::deliver-messages!
                 (fn [_ _ _ ns]
                   (events/dispatch-event
                    emitter ::messenger-interface/message-delivered ns)))
      (events/add-observer
       emitter
       ::api/message-post
       ::terminal-messenger
       (fn [[{:keys [text]}]]
         (messenger-put-message! terminal-messenger-atom text :incoming)
         (deliver-messages!)))
      terminal-messenger-atom))

(defmethod ig/init-key ::thread-pool [_ {:keys [pool-size]}]
  (alter-var-root #'artificial-parrot.async/*default-thread-pool*
                  (constantly (artificial-parrot.async/create-thread-pool pool-size)))
  artificial-parrot.async/*default-thread-pool*)

(defmethod ig/halt-key! ::thread-pool [_ thread-pool]
  (.shutdown thread-pool))

(defmethod ig/init-key ::emitter [_ {:keys [opts]}]
  (events/create-emitter opts))

(defmethod ig/halt-key! ::emitter [_ emitter]
  (when emitter
    (def emitter)
    (events/stop-listening emitter)))

(defmethod ig/init-key ::println-notifier [_ {:keys [opts emitter] :as args}]
  (events/add-observer
   emitter
   ::messenger-interface/message-delivered
   ::println-notifier
   (fn [[message]]
     (notify-message! opts [message])))
  args)

(defmethod ig/init-key ::println-notifier [_ {:keys [opts emitter]}]
  (events/remove-observer
   emitter
   ::messenger-interface/message-delivered
   ::println-notifier))

(defmethod ig/init-key ::http-listeners [_ {:keys [emitter]}]
  (let [http-listeners (atom {})]
    (events/add-observer
     emitter
     ::api/webhook-listener-post
     ::http-listeners
     (fn [[{:keys [id] :as webhook-listener}]]
       (swap! http-listeners
              (fn [listeners]
                (assoc listeners id webhook-listener)))))
    (events/add-observer
     emitter
     ::api/webhook-listener-delete
     ::http-listeners
     (fn [[id]]
       (swap! http-listeners (fn [listeners] (dissoc listeners id)))))
    {:http-listeners http-listeners
     :emitter emitter}))

(defmethod ig/halt-key! ::http-listeners [_ {:keys [emitter]}]
  (events/remove-observer
   emitter
   ::api/webhook-listener-post
   ::http-listeners)
  (events/remove-observer
   emitter
   ::api/webhook-listener-delete
   ::http-listeners))

(defmethod ig/init-key ::http-notifier [_ {:keys [opts emitter] :as args
                                           {:keys [http-listeners]} :http-listeners}]
  (events/add-observer
   emitter
   ::api/message-post
   ::messenger-interface/message-delivered
   (fn [[message]]
     (doseq [[_ listener] @http-listeners]
       (execute-async #(notify-message! (merge opts listener) [message]) nil))))
  args)

(defmethod ig/init-key ::http-notifier [_ {:keys [emitter]}]
  (events/remove-observer
   emitter
   ::api/message-post
   ::messenger-interface/message-delivered))

(defmethod ig/init-key ::handlers [_ {:keys [emitter]}]
  (get-handlers {:emitter emitter}))

(defmethod ig/init-key ::router [_ {:keys [handlers]}]
  (get-router {:handlers handlers}))

(defmethod ig/init-key ::server [_ {:keys [router port]}]
  (start-server {:router router :port port}))
(defmethod ig/halt-key! ::server [_ server]
  (when server (.stop server)))

(comment
  (clojure.tools.namespace.repl/refresh-all)
  (clojure.tools.namespace.repl/refresh))

(comment
  (clear)
  (prep)
  (init)
  (reset))

(alias 'api-handlers 'artificial-parrot.messenger.api.handlers)

(comment
  (def server (get integrant.repl.state/system ::server))
  (def router (get integrant.repl.state/system ::router))
  (def app (artificial-parrot.messenger.api/app router))
  (def handlers (get integrant.repl.state/system ::handlers))
  (def emitter (get integrant.repl.state/system ::emitter))
  (def http-listeners (get integrant.repl.state/system ::http-listeners))
  ;; (def app artificial-parrot.messenger.api/instance-app)

  (-> (ring-mock/request :post "/api/message")
      (ring-mock/json-body {:text "olá de volta"})
      (ring-mock/header "Accept" "application/json")
      (->> (def mock-request) deref)
      app
      (->> (def resp) deref))

  (reitit.core/match-by-path router "/api/message")

  (app ring.adapter.jetty/request-map)


  (-> 
   ;; (ring-mock/request :post "api/message")
   ;; (ring-mock/json-body {:text "olá de volta"})
   ;; (ring-mock/header "Accept" "application/json")
   ;; (->> (def mock-request) deref)
   ;; app
   ring.adapter.jetty/request-map
   :body
   ;; .getInputStream
   (java.io.InputStreamReader. java.nio.charset.StandardCharsets/UTF_8)
   (java.io.BufferedReader.)
   (.lines)
   (.collect (java.util.stream.Collectors/toList))
   (->> (java.lang.String/join "\n"))
   (->> (def request-500-problem) deref))

  (apply (::api-handlers/message-post handlers) [{:parameters {:body { :text "olá de volta"}}}])
  )
