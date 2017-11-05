(ns triql.core
  (:import (java.net URI))
  (:require [clojure.spec.alpha :as s]))

(s/def ::uri uri?)

(deftype Variable [name])
(defn ? [n] (->Variable n))
(s/def ::variable-ref (partial instance? Variable))

(defmulti encode-literal class)
(defmethod encode-literal String [s]
  {:value s :datatype "http://www.w3.org/2001/XMLSchema#string"})

(defmethod encode-literal Long [s]
  {:value s :datatype "http://www.w3.org/2001/XMLSchema#integer"})

(defmethod encode-literal :default [s]
  (do ::s/invalid))

(s/def ::literal (s/conformer encode-literal))


(s/def ::triple-subject (s/or ::uri ::uri
                              ::variable-ref ::variable-ref))
(s/def ::triple-predicate (s/or ::uri ::uri
                                ::variable-ref ::variable-ref))
(s/def ::triple-object (s/or ::uri ::uri
                             ::variable-ref ::variable-ref
                             ::literal ::literal))

(s/def ::triple (s/tuple ::triple-subject
                         ::triple-predicate
                         ::triple-object))

(s/def ::triples (s/coll-of ::triple))

(s/def ::group (s/keys :req [::triples]))

(s/def ::pattern ::group)

(defn group [& triples]
  {::triples triples})

#_
(s/conform ::group (group [(URI. "http://w.com")
                          (URI. "http://x.com")
                          (URI. "http://y.com")]
                         [(URI. "http://a.com")
                          (URI. "http://b.com")
                          34 #_
                          (URI. "http://c.com")]))


(comment

(def ds (open-datastore "http://localhost:3030/ds/"))

(query
 ds
 (solve
  (pattern [(? :foo) :foaf:knows (? :bar)])))

(query
 ds
 (bind [:ex:name '(strcat ?first ?last)]
       (solve
        (pattern [(? :foo) :foaf:knows (? :bar)])))

(query
 ds
 (construct
  [[(? :bar) :ex:famousTo (? :foo)]
   [(? :foo) :ex:heardOf (? :bar)]]
  (solve
   (pattern [(? :foo) :foaf:knows (? :bar)]))))

(query
 ds
 (insert
  [[(? :bar) :ex:famousTo (? :foo)]
   [(? :foo) :ex:heardOf (? :bar)]]
  (solve
   (pattern [(? :foo) :foaf:knows (? :bar)]))))
))
