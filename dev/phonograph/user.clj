(ns phonograph.user
  (:require [clojure.java.io :as io]
            [phonograph.core :as pg :refer :all]
            [phonograph.db :as db]))

(def app (atom {:db {:tdb-location
                     (.toString (.getAbsoluteFile (io/file "db")))
                     }}))

(defn run []
  (swap! app pg/start-all))
