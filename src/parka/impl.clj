(ns parka.impl
  (require [clojure.java.io :as io])
  (require [clojure.zip :as zip])
  (require [clojure.data.xml :as xml])
  (require [clojure.data.zip.xml :as zip-xml]))

(def time-format
  "EEE, d MMM yyyy HH:mm:ss z")
(def time-formatter
  (new java.text.SimpleDateFormat time-format java.util.Locale/ENGLISH))

(defn ->time
  [input]
  (.parse time-formatter input ))

(defn get-path [f] (.getPath f))

(defn remove-non-digits
  [input]
  (apply str (filter #(Character/isDigit %) input)))

(defn fix-gmt
  [input]
  (str (clojure.string/replace input "GMT +" "GMT+0") ":00"))

(defn parse-xml
  [xml-file]
  (xml/parse-str (slurp xml-file :encoding "UTF-8")))

(defn z [xml-parsed]
  (zip/xml-zip xml-parsed))

(defn item->map
  [item]
  {:name   (apply str (zip-xml/xml-> item :title zip-xml/text))
   :free (. Integer parseInt (remove-non-digits (apply str (zip-xml/xml-> item :description zip-xml/text))))
   :time (.getTime (->time (fix-gmt (apply str (zip-xml/xml-> item :pubDate zip-xml/text)))))})


(defn rss->map
  [input]
  (let [root (z (parse-xml input))]
    (mapv item->map (zip-xml/xml-> root :channel :item))))
