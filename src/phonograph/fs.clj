(ns phonograph.fs
  (:require [phonograph.db :as db]
            [phonograph.sha256 :as sha256]
            [clojure.java.io :as io]))


(defn visit-file [ds name]
  (let [expected (db/get-file-sha256 ds name)
        ;; XXX does this leak open files?
        actual (sha256/digest-stream (io/input-stream name))]
    (if (not (and expected (= expected actual)))
      (db/set-file-sha256 ds name actual))))

(defn- get-directory-entries [pathname]
  (map (fn [f] {:name f :directory? (.isDirectory f)
                :last-modified (.lastModified f) })
       (.listFiles (io/file pathname))))

(defn visit-folder [ds name]
  (let [last-scan-time (or (db/get-file-scanned-timestamp ds name) 0)
        next-scan-time (.getTime (java.util.Date.))]
    (run! (fn [stat]
            (if (:directory? stat)
              (visit-folder ds (:name stat))
              (visit-file ds (:name stat))))
          (filter #(> (:last-modified %) last-scan-time)
                  (get-directory-entries name)))
    (db/set-file-scanned-timestamp ds name next-scan-time)))
