(ns user.dalloca
  (:require [integrant.core :as ig]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [messenger.conversational-interface :refer [terminal-messenger put-message messenger-put-message! messenger-deliver!]]
            
            [user.messenger.println-listener]))

(comment (in-ns 'user.dalloca))

(comment (integrant.repl/set-prep! (constantly user.messenger.println-listener/config)))

