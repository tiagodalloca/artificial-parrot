(ns artificial-parrot.messenger.notifier.stdout
  (:require [artificial-parrot.messenger.notifier :refer [notify-message!]]))

(defmethod notify-message! ::stdout-notifier
  stdout-notify-message!
  [opts messages]
  (doseq [m messages]
    (println (str "stdout-notify-message!> " m))))
