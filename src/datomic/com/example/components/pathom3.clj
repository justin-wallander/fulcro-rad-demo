(ns com.example.components.pathom3
  (:require [com.wsscode.pathom3.cache :as p.cache]
            [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
            [com.wsscode.pathom3.connect.built-in.plugins :as pbip]
            [com.wsscode.pathom3.connect.foreign :as pcf]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.connect.operation.transit :as pcot]
            [com.wsscode.pathom3.connect.planner :as pcp]
            [com.wsscode.pathom3.connect.runner :as pcr]
            [com.wsscode.pathom3.error :as p.error]
            [com.wsscode.pathom3.format.eql :as pf.eql]
            [com.wsscode.pathom3.interface.async.eql :as p.a.eql]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.wsscode.pathom3.interface.smart-map :as psm]
            [com.wsscode.pathom3.format.shape-descriptor :as pf.sd]
            [com.wsscode.pathom3.path :as p.path]
            [com.wsscode.pathom3.plugin :as p.plugin]))


   (defn query-tester
     [query & args]
     (db/with-transaction neo4j-gcp--bolt-db tx
       (vec (query tx (first args)))))


(pco/defresolver full-name [{::keys [first-name last-name]}]
  {::full-name (str first-name " " last-name)})

(def indexes (pci/register full-name))

(def person-data {::first-name "Anne" ::last-name "Frank"})

(def smart-map (psm/smart-map indexes person-data))

; if you lookup for a key in the initial data, it works the same way as a regular map
(::first-name smart-map) ; => "Anne"

; but when you read something that's not there, it will trigger the Pathom engine to
; fulfill the attribute
(::full-name smart-map)


(comment
  ;; pathom shape descriptor will be useful
  {:user/name         {}
   :user/billing-card {:card/number {}}
   :user/friends      {:user/id {}}})