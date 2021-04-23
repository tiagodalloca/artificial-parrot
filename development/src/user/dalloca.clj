(ns user.dalloca
  (:require [artificial-parrot.messenger.conversational-interface
             :refer
             [messenger-deliver!
              messenger-put-message!
              put-message
              terminal-messenger]]
            [clj-http.client :as http-client]
            [clojure.data.json :as json]
            [integrant.core :as ig]
            [integrant.repl :refer [clear go halt init prep reset reset-all]]
            [user.messenger.println-listener
             :refer
             [deliver-messages! send-message!]]
            [artificial-parrot.events :refer :all]))

(comment (in-ns 'user.dalloca))

(comment
  (clojure.tools.namespace.repl/refresh-all)
  (clojure.tools.namespace.repl/refresh))

(comment (integrant.repl/set-prep! (constantly user.messenger.println-listener/config)))

(comment
  (def test-emitter (create-emitter {:pool-size 1
                                     :chan-buf-size 10
                                     :immediately-start? false}))
  (start-listening test-emitter)
  (add-observer test-emitter :oi :println-obs println)
  (dispatch-event test-emitter [:oi "oi"])
  (remove-observer test-emitter :oi :println-obs)
  (stop-listening test-emitter)
  test-emitter)

