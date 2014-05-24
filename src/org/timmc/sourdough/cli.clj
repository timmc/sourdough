(ns org.timmc.sourdough.cli
  "Entrance namespace for CLI."
  (:require [org.timmc.sourdough.core :as sd]
            [org.timmc.handy :as handy]
            [slingshot.slingshot :refer [try+ throw+]]))

(def exit-codes
  {:normal 0
   :generic-error 1
   :bad-request 2
   :bad-environment 3})

;;;; Dispatch

(defn run-action
  "Run the named action with the given args. Throws:

- :org.timmc.sourdough/unknown-action
- :org.timmc.sourdough/invalid-action-args"
  [action args]
  (if-let [action-var (get sd/actions action)]
    (if (handy/matching-arity action-var (count args))
      (apply action-var args) ;; TODO: Traits for pre/post?
      (throw+ {:type :org.timmc.sourdough/invalid-action-args}))
    (throw+ {:type :org.timmc.sourdough/unknown-action})))

(def usage-aliases
  "Arglists that really mean a usage request."
  #{[] ["--help"]})

(defn run
  "Run action specified command line arguments, yielding an exit type keyword."
  [cli-args]
  (let [[action & args] (if (.contains usage-aliases (vec cli-args))
                          ["usage"]
                          cli-args)]
    (try+
     ;; usage-aliases guarantees we  always have a first argument
     (run-action action args)
     :normal
     (catch [:type :org.timmc.sourdough/unknown-action] {:as m}
       (println "Unknown action:" action)
       :bad-request)
     (catch [:type :org.timmc.sourdough/invalid-action-args] {:as m}
       (println (format "Cannot call action '%s' with %s arguments."
                        action (count args)))
       :bad-request))))

(defn -main
  "CLI entrance point."
  [& cli-args]
  (try+
   (let [exit-key (run cli-args)]
     (if-let [exit-code (get exit-codes exit-key)]
       (System/exit exit-code)
       ;; TODO: be more careful about potentially printing a weird
       ;; type? But this should be statically preventable.
       (do (println "Unknown exit type:" exit-key)
           (System/exit (:generic-error exit-codes)))))
   (catch Throwable t
     (.printStackTrace t)
     (System/exit (:generic-error exit-codes)))))
