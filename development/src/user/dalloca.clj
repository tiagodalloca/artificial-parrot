(ns user.dalloca
  (:require [integrant.core :as ig]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [messenger.conversational-interface :refer [terminal-messenger put-message messenger-put-message! messenger-deliver!]]
            
            [clj-http.client :as http-client]
            [clojure.data.json :as json]

            [user.messenger.println-listener :refer [send-message! deliver-messages!]]))

(comment (in-ns 'user.dalloca))

(comment (integrant.repl/set-prep! (constantly user.messenger.println-listener/config)))

