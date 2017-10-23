(ns phonograph.db
  (:require [clojure.java.io :as io])
  (:import (org.apache.jena.query DatasetFactory Dataset)
           (org.apache.jena.rdf.model Model)
           (org.apache.jena.tdb TDBFactory)
           #_ (org.apache.jena.fuseki.embedded FusekiServer)))

(defn open-dataset []
  (let [dir (System/getProperty "user.dir")]
    (TDBFactory/createDataset (.toString (io/file dir "db")))))

(def ds (atom nil))

(reset! ds (open-dataset))

#_
(defn open-server [ds]
  (doto
      (-> (FusekiServer/create)
          (.add "/rdf" ds)
          .build)
    (.start)))
