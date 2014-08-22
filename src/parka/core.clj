(ns parka.core
  (require [clojure.java.io :as io])
  (require [clojure.core.async :as async])
  (require [parka.async :as p])
  (require [capacitor.core :as influx])
  (:gen-class))

(def db-name "parka")

(def file-name "D:\\dev\\linode\\data_fresh\\pls-complete\\0\\file-0101140016.xml")
(def file-name2 "D:\\dev\\linode\\data_fresh\\pls-complete\\0\\file-0101140000.xml")

(def root-dir (io/file "D:\\dev\\linode\\data_fresh\\pls-complete\\"))

(def sub-dirs (drop 1 (file-seq root-dir)))





(def client
  (influx/make-client {:db db-name}))

;;(defn accu
;;  [files]
;;  (flatten (concat (mapv rss->map files))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (p/insert-to-db (p/accumulator (p/parse-results (p/file-producer sub-dirs)) 300 5000) client "parka"))

  ;;(clojure.pprint/pprint (accu (mapv get-path (drop 1 (file-seq root-dir))))))