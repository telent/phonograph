(ns phonograph.core
  (:require [phonograph.db :as db]))

(def components
  [[[:db] db/control]
   [[:folders] (fn [s op] (println s) s)]])

(defn start-all [config]
  (reduce (fn [c [ks f]]
            (update-in c ks f :start))
          config
          components))

(defn stop-all [config]
  (reduce (fn [c [ks f]]
            (update-in c ks f :stop))
          config
          (reverse components)))



(defn -main [ & args ]
  (println "hello world"))
