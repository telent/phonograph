(ns phonograph.db
  (:require [clojure.java.io :as io])
  (:import (org.apache.jena.query
            DatasetFactory Dataset
            QueryExecution
            QueryExecutionFactory
            QueryFactory)
           (java.net URI)
           (org.apache.jena.rdf.model Model)
           (org.apache.jena.tdb TDBFactory)
           (org.apache.jena.update UpdateAction)
           #_ (org.apache.jena.fuseki.embedded FusekiServer)))

(defn open-dataset []
  (let [dir (System/getProperty "user.dir")]
    (TDBFactory/createDataset (.toString (io/file dir "db")))))

(def ds (atom nil))

(defn graph-size-in [ds]
  (.size (.getGraph (.getDefaultModel ds))))


(defn update-dataset [dataset s]
  (UpdateAction/parseExecute s dataset))

(defn futz [ri]
  (cond (.isLiteral ri) (.asLiteral ri)
        (.isURIResource ri) (URI. (.getURI (.asResource ri)))
        (.isResource ri) (.asResource ri)
        :else ri))


(defn collect-results [rs]
  (when (.hasNext rs)
    (let [sol (.next rs)
          names (iterator-seq (.varNames sol))]
      (conj (or (collect-results rs) [])
            (reduce (fn [m n] (assoc m n (futz (.get sol n))))
                    {}
                    names)))))

(defn select-from-dataset [dataset s]
  (let [q (QueryFactory/create s)
        m (.getDefaultModel dataset)
        qe (QueryExecutionFactory/create q m)]
    (try (let [rs (.execSelect qe)]
           (doall (collect-results rs))))))


(reset! ds (open-dataset))


#_
(defn open-server [ds]
  (doto
      (-> (FusekiServer/create)
          (.add "/rdf" ds)
          .build)
    (.start)))
