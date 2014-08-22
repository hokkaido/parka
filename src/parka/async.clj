(ns parka.async
  (require [parka.impl :as impl])
  (require [capacitor.core :as influx])
  (require [clojure.core.async :refer [chan >! <! timeout go go-loop]]))


(defn accumulator
  "Accumulates results from one channel up to a given batch-size
  and supplies them to another channel.  If timeout in msecs is reached,
  the already batched results will be sent."
  [from batch-size msecs]
  (let [to (chan)]
  (go-loop [batch [] begin (System/currentTimeMillis)]
    (if (> (- (System/currentTimeMillis) begin) msecs)
      (when-not (empty? batch)
        (>! to batch))
      (if (> batch-size (count batch))
        (let [v (<! from)]
          (if (nil? v)
            (when-not (empty? batch)
              (>! to batch))
            (recur (concat batch v) (System/currentTimeMillis))))
        (do
          (>! to batch)
          (recur [] (System/currentTimeMillis))))))
  to))

(defn parse-results
  "Parses"
  [in]
  (let [out (chan)]
  (go-loop [v (<! in)]
    (when v
        (>! out (impl/rss->map v))
        (recur (<! in))))
  out))

(defn insert-to-db
  [in client db-name]
  (go-loop [v (<! in)]
           (when v
             (try
               (influx/post-points client db-name (flatten v))
                  (catch Exception e
                    (spit "debug.log" (with-out-str (pr v)) :append true)))
               (recur (<! in)))))

(defn file-producer
  [sub-dirs]
  (let [out (chan)]
  (go
    (doseq [sub-dir sub-dirs]
      (let [files (drop 1 (mapv impl/get-path (file-seq sub-dir)))]
        (doseq [file files]
          (>! out file)))))
  out))



