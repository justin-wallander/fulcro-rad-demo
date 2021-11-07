(ns com.example.components.fulcro-comps-v1
  (:require #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
               :cljs [com.fulcrologic.fulcro.dom :as dom])))


(defsc Person [this {:person/keys [name age]}]
  (dom/div
   (dom/p "Name: " name)
   (dom/p "Age: " age)))

(def ui-person (comp/factory Person))


(defsc Root [this props]
  (dom/div
   (ui-person {:person/name "Joe" :person/age 22})))