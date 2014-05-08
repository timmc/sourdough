(ns org.timmc.sourdough.core
  "Core namespace, defining actions."
  (:require (org.timmc.sourdough [history :as h]
                                 [git :as g])))

(declare actions)

;;;; Shortcuts

(defn append-to-history
  "Append lines to history and commit with message."
  [lines message]
  (g/append-and-stage "history.log")
  (g/commit-with-message message))

;;;; Traits

;;; Traits are shorthand ways of attaching validators, descriptions,
;;; etc. to a set of actions.

;;;; Actions

;;; Actions are embodied as vars with metadata.

(defn
  ^{:sdesc "Print a usage guide."
    :ldesc "Print a list of actions with their arguments and a short description. If given an action name as an argument, print the arguments, short description, and long description."
    :args "[action]"}
  print-usage
  ([]
     (println "Usage: <action> [args ...]")
     (doseq [act-name (sort (keys actions))]
       (let [metadata (meta (get actions act-name))
             arg-string (:args metadata "[args ...]")
             desc-string (:sdesc metadata "(no description)")]
         (println (format "  %s %s: %s"
                          act-name arg-string desc-string)))))
  ([action]
     (if-let [action-var (get actions action)]
       (let [metadata (meta action-var)
             sdesc (:sdesc metadata)
             ldesc (:ldesc metadata)]
         (println (str "Usage for action " action ":"))
         (println (str "Arguments: " (:args metadata "[args ...]")))
         (when sdesc
           (println sdesc))
         (when (and sdesc ldesc)
           (println))
         (when ldesc
           (println ldesc))))))

(defn
  ^{:sdesc "Initialize the current directory as a sourdough repo."
    :ldesc "This action is idempotent and will not affect an existing repo."}
  init-repo
  [args]
  (g/init))

(defn
  ^{:sdesc "Track a new line of sourdough starter"
    :ldesc "Usage: new <name>"}
  new-line
  [args]
  )

(defn
  ^{:sdesc "Add ingredients to the starter"
    :ldesc "Usage: add [--when <datetime>] [--] [<what> <quantity>] *
Quantity may be a number followed by a unit of mass, weight, or volume
or a question mark indicating an unknown amount."}
  add-ingredients
  [args]
  ;;TODO
  )

(defn
  ^{:sdesc "Add a note in the history"
    :ldesc "Usage: note [--when <datetime>] [--] <note>"}
  add-note
  [args]
  ;;TODO
  )

(def actions
  "Map of action names to vars."
  {"usage" #'print-usage
   "init" #'init-repo
;;TODO   "lines" #'show-lines
   "new" #'new-line
;;TODO   "rename" #'rename-line
   "add" #'add-ingredients
   "note" #'add-note})

