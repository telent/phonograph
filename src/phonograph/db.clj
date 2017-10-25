(ns phonograph.db
  (:require [clojure.java.io :as io]
            [speckled.dsl :as speckled :refer :all]
            [speckled.rdf :as rdf :refer [u]])
  (:import (org.apache.jena.query
            DatasetFactory Dataset
            QueryExecution
            QueryExecutionFactory
            QueryFactory)
           (java.net URI)
           (org.apache.jena.rdf.model Model)
           (org.apache.jena.tdb TDBFactory)
           (org.apache.jena.update UpdateAction)))

(defn- graph-size-in [ds]
  (.size (.getGraph (.getDefaultModel ds))))

(defn update-dataset [dataset s]
  (UpdateAction/parseExecute s dataset))

(defmulti convert-literal (fn [l] (.getDatatypeURI l)))
(defmethod convert-literal "http://www.w3.org/2001/XMLSchema#boolean" [l]
  (.getBoolean l))
(defmethod convert-literal "http://www.w3.org/2001/XMLSchema#integer" [l]
  (.getLong l))
(defmethod convert-literal "http://www.w3.org/2001/XMLSchema#string" [l]
  (.getString l))
(defmethod convert-literal :default [l] l)

(defn- convert-resource [ri]
  (cond (.isLiteral ri) (convert-literal (.asLiteral ri))
        (.isURIResource ri) (URI. (.getURI (.asResource ri)))
        (.isResource ri) (.asResource ri)
        :else ri))

(defn- convert-result [sol]
  (let [names (iterator-seq (.varNames sol))]
    (reduce (fn [m n] (assoc m n (convert-resource (.get sol n))))
            {}
            names)))

(defn select-from-dataset [dataset s]
  (let [q (QueryFactory/create s)
        m (.getDefaultModel dataset)
        qe (QueryExecutionFactory/create q m)]
    (try (let [rs (.execSelect qe)]
           (doall (map convert-result (iterator-seq rs))))
         (finally (.close qe)))))

(defn query [dataset q]
  (binding [rdf/prefixes (assoc rdf/prefixes
                                "phono" "http://phono.telent.net/rdf#")]
    (let [s (->string q)]
      (println s)
      (select-from-dataset dataset s))))

(defn update [dataset q]
  (binding [rdf/prefixes (assoc rdf/prefixes
                                "phono" "http://phono.telent.net/rdf#")]
    (let [s (->string q)]
      (UpdateAction/parseExecute s dataset))))

;;;;;;;;


(defn set-file-sha256 [ds file value]
  (let [subject (.toURI (io/file file))
        object (URI. (str "sha256:" value))]
    (update ds (substitute
                (group [subject :phono:digest (? :old)])
                (group [subject :phono:digest object])
                (group (optional (group [])
                                 (group [subject :phono:digest (? :old)])))))))

(defn get-file-sha256 [ds name]
  (-> (query ds
             (group [(.toURI (io/file name)) :phono:digest (? :digest)]))
      first
      (get "digest")))

;; we expect timestamps to be raw number of ms since 1970, because the only
;; thing we do with them is compare against java.io.File#lastModified
(defn get-file-scanned-timestamp [ds name]
  (-> (query
       ds
       (group [(.toURI (io/file name)) :phono:lastScanned (? :lastScanned)]))
      first
      (get "lastScanned")))

(defn set-file-scanned-timestamp [ds file value]
  (let [subject (.toURI (io/file file))
        op :phono:lastScanned]
    (update ds (substitute
                (group [subject op (? :old)])
                (group [subject op value])
                (group (optional (group [])
                                 (group [subject op (? :old)])))))))

(defn get-files-without-tags [ds]
  (query
   ds
   (filter-solns
    (optional (group [(? :name) :phono:digest (? :sha)])
              (group [(? :name) :phono:fileType (? :fileType)]))
    '(! (bound ?fileType)))))



;;;;;;;;
(defmulti control (fn [state op] op))

(defmethod control :start [state op]
  (let [ds (TDBFactory/createDataset (:tdb-location state))]
    (assoc state :dataset ds)))

(defmethod control :stop [state op]
  (let [ds (get state :dataset)]
    (.close ds)
    (dissoc state :dataset)))
