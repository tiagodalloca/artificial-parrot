(ns artificial-parrot.messenger.notifier)

(defmulti notify-message!
  (fn [opts messages]
    (get opts :type)))

(comment
  (notify-message! {:type :messenger.notifier.http/http-notifier}
                   [{:text "blah"}]))
