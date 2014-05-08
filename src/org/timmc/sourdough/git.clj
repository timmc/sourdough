(ns org.timmc.sourdough.git
  (:require [clj-jgit.porcelain :as g]))

(defn init
  []
  (g/git-init))

(defn append-and-stage
  "Append lines to file and stage changes."
  [path lines]
  (let [write (apply str (map #(str % "\n") lines))]
    (spit path write :append true))
  (let [repo (g/load-repo ".")]
    (g/git-add repo path)))

(defn commit-with-message
  [message]
  (let [repo (g/load-repo ".")]
    (g/git-commit repo message)))

