(ns org.timmc.sourdough.cli
  "Entrance namespace for CLI."
  (:require [org.timmc.sourdough.core :as sd]))

(def exit-codes
  {:normal 0
   :generic-error 1
   :unknown-action 2})

;;;; Dispatch

(defn run-action
  [action-name props])

(def usage-aliases
  "Arglists that really mean a usage request."
  #{[] ["--help"]})

(defn -main
  "CLI entrance point."
  [& cli-args]
  (try
    (let [[action & args] (if (.contains usage-aliases (vec cli-args))
                            ["usage"]
                            cli-args)]
      (if-let [action-var (get sd/actions action)]
        (do (apply action-var args) ;; TODO: Traits
            (System/exit (:normal exit-codes)))
        (do (println "Unknown action:" action)
            (System/exit (:unknown-action exit-codes)))))
    (catch Throwable t
      (println t)
      (System/exit (:generic-error exit-codes)))))
