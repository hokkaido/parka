(ns parka.helper
  (require [clojure.java.io :as io])
  (require [parka.core :as core]))

(defn pp
  [xml-file]
  (clojure.pprint/pprint (core/rss->map xml-file)))

(defn pp-dir
  [dir-str]
  (let [dir (io/file dir-str)]
    (clojure.pprint/pprint (core/accu (mapv core/get-path (drop 1 (file-seq dir)))))))
