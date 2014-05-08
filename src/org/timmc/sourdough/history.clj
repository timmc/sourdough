(ns org.timmc.sourdough.history
  "History file format API.

Methods starting with 'f' are formatters, taking data and producing a
one-line string, and methods starting with 'p' are parsers, doing the
opposite.

Formatters do not add newlines to the end of their output. Formatters
that produce multiple lines of output generally do so by emitting a
seq of strings instead of a newline-delimited string. (`f-comment` is
an exception to this rule.)

For documentation of file format, see history-file.md in doc folder."
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [clj-time.core :as time]
            [clj-time.format :as time-f]))

;;;; Utilities

(def iso8601 (time-f/formatters :date-time))

(defn timestamp
  "Produce the current UTC timestamp in ISO 8601 format (or use
specified Joda DateTime."
  ([] (time/now))
  ([when] (time-f/unparse iso8601 when)))

(defn assoc-if
  "Assoc [k v] onto m if v is truthy."
  [m k v]
  (if v (assoc m k v) m))

;;;; API

(defn f-comment
  "Convert a potentially multiline message into a comment, also
potentially multi-line."
  [msg]
  (str/join "\n" (map #(str "# " %) (str/split-lines msg))))

(defn f-format
  "Yield an appropriate format line."
  []
  (json/write-str ["format" {:spec "timmc.sourdough", :version "1"}]))

(defn f-add
  "Yield an event-add line."
  [substance quantity & [{:keys [when]}]]
  (let [payload (assoc-if {:substance substance, :quantity quantity}
                          :when (timestamp when))]
    (json/write-str ["event" "add" payload])))

(def tsd-name "tsd:name")

(defn f-describe
  "Yield a description state line."
  [key val & [{:keys [when]}]]
  (let [payload (assoc-if {:value val} :when (timestamp when))]
    (json/write-str ["state" "describe" key payload])))

(defn f-note
  "Yield a note line."
  [note & [{:keys [when]}]]
  (let [payload (assoc-if {:value note} :when (timestamp when))]
    (json/write-str ["note" payload])))
