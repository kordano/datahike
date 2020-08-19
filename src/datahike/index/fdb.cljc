(ns datahike.index.fdb
  (:require [datahike.datom :as dd]
            [fdb.core :as fc]
            [fdb.keys :as fk])
  )


(defn- index-type->datom-fn [index-type]
  (case index-type
    :aevt (fn [[a e v tx]] (dd/datom e a v tx true))
    :avet (fn [[a v e tx]] (dd/datom e a v tx true))
    (fn [[e a v tx]] (dd/datom e a v tx true))))

(defn -slice [db from to index-type]
  (map (index-type->datom-fn index-type)
    (fc/get-range index-type from to)))

(defn -all
  [db index-type]
  (map (index-type->datom-fn index-type)
    (fc/get-range index-type
      [:dh-fdb/min-val :dh-fdb/min-val :dh-fdb/min-val :dh-fdb/min-val]
      [:dh-fdb/max-val :dh-fdb/max-val :dh-fdb/max-val :dh-fdb/max-val])))

(defn -seq [db]
  (seq (-all db :eavt)))

(defn -count [db]
  (count (-all db :eavt)))


(def -flush identity)

;; TODO: implement? Ask about semantic!
(def -transient identity)
(def -persistent! identity)


(defn -insert [db datom index-type]
  (let [db-after (fc/insert index-type datom)]
    db))

(defn -remove [db datom index-type]
  (let [_ (fc/clear index-type datom)]
    db))

;; Called 3 times, one for each index.
(defn empty-db [index-type]
  (let [db (fc/empty-db)]
    ;; (println "...db is nil?:" db)
    db))