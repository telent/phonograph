(ns phonograph.sha256
  (:import [java.io BufferedInputStream]))

(defn digest-stream [stream]
  (let [buffer (byte-array 8192)
        digest (java.security.MessageDigest/getInstance "SHA-256")
        bstream (BufferedInputStream. stream)]
    (loop []
      (let [count (.read bstream buffer)]
        (when (> count 0)
          (.update digest buffer 0 count)
          (recur))))
    (apply str (map #(format "%02x" %)
                    (.digest digest)))))
