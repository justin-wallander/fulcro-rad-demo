(ns com.example.components.neo4j
  "neo4j practice run, but probs will just iterate on it so that it will be long term"
  
  (:require
   [neo4j-clj.core :as db]
   [org.httpkit.client :as http]
   [clojure.data.json :as json]
  ;;  [org.httpkit.sni-client :as http]
   [mount.core :refer [defstate]])
  (:import (java.net URI)))
    


    ;; (def neo4j-bolt-db
    ;;   (db/connect (URI. "bolt://35.237.229.3:7687")
    ;;               "jwall"
    ;;               "2688")) 

    (def neo4j-gcp--bolt-db
      (db/connect (URI. "bolt://34.72.52.231:7687")
                  "neo4j"
                  "jwall")) 
    
    (defstate neo4j-db :start (neo4j-bolt-db))

    ;; (http/get "http://host.com/path")
    (def token "secret_Eh9LEwrFvdKJsHLygDSX7QRMCwOV8RnlK4WSKUE6NGn")
    (def notion-customer-db-id "de297bebecf84283bafa4a51b174f326")

;; (def options {             ; ms
              ;; :basic-auth ["user" "pass"]
              ;; :query-params {:param "value" :param2 ["value1" "value2"]}
              ;; :user-agent "User-Agent-string"
              ;; :headers {"Authorization" (str "Bearer " token) "Notion-Version" "2021-08-16" "Content-Type" "application/json"}})
    
        

;; @(http/post (format "https://api.notion.com/v1/databases/%s/query" notion-customer-db-id) options
          ;; (fn [{:keys [status headers body error]}] ;; asynchronous response handling
            ;; (if error
              ;; (println "Failed, exception is " error)
              ;; (println "Async HTTP GET: " status))))
    
    
    

  ;; (let [options {:headers {"Authorization" (str "Bearer " token) "Notion-Version" "2021-08-16" "Content-Type" "application/json"}}
        ;; {:keys [status headers body error]} (http/post (format "https://api.notion.com/v1/databases/%s/query" notion-customer-db-id) options)]
    ;; (if error
      ;; (println "Failed, exception is " error)
      ;; (println "Async HTTP POST: " status)))
        
        ;; this is working
        
  (def notion-customers(let [options {:headers {"Authorization" (str "Bearer " token) "Notion-Version" "2021-08-16" "Content-Type" "application/json"}}
        {:keys [status headers body error] :as resp} @(http/post (format "https://api.notion.com/v1/databases/%s/query" notion-customer-db-id) options)]
    (if error
      (println "Failed, exception: " error)
      (json/read-str body))))
   (defn from-notion-idx-customer-email [& args] (get-in (((((get-in (first args) ["results" (first (rest args)) ]) "properties") "personEmail") "rollup") "array") [(first (rest args)) "email"]))
notion-customers
  (def from-notion-idx-customer-email (get-in (((((get-in notion-customers ["results" 0]) "properties") "personEmail") "rollup") "array") [0 "email"]))
;; (from-notion-idx-customer-email [notion-customers 0])
  from-notion-idx-customer-email

  
  (keys ((get-in notion-customers ["results" 0]) "properties"))


  (comment
    ;; save this for another time, go with http-kit for now
    (:require-macros [cljs.core.async.macros :refer [go]])
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]

    (def local-db
      (db/connect (URI. "bolt://35.237.229.3:7687")
                  "jwall"
                  "2688"))

    "CALL dbms.changePassword('jwall');" | cypher-shell -a $IP -u neo4j -p "neo4j"
    gcloud config set project <project-id>
    gcloud compute instances create my-neo4j-instance --image-project launcher-public --image neo4j-community-1-4-3-2-gds-apoc --tags neo4j




    (db/defquery all_nodes
      "Match (n)  return n{.*} as Admin")


    (db/defquery lower-case-emails
      "MATCH (u:User) RETURN u.email as user/email")

    (db/defquery match-user-email
      "MATCH (u:User {email: $email}) RETURN u as user")

    (db/defquery match-user-has-page-email
      "MATCH (u:User {email: $email})-[:HAS_PAGE]->(p:Page) RETURN u as user, p as page")

    (db/defquery match-user-email
      "MATCH (u:User {email: $email}) RETURN u as user")

    (db/defquery get-all-users
      "MATCH (u:User) RETURN u as user")

    (db/defquery get-all-users2
      "MATCH p = (u:User) return nodes(p)")


    ;; (db/defquery show_dbs
      ;; ":USE hub")
    ;;TODO good stopping point for the night, will come back tomorrow
    (defn activity-node-count
      [& args]
      (db/with-transaction local-db tx
        (apply hash-map (args tx))))

    (defn query-tester
      [query & args]
      (db/with-transaction neo4j-gcp--bolt-db tx
        (vec (query tx (first args)))))

    (defn get-user-by-email
      [& args]
      (db/with-transaction local-db tx
        (vec (match-user-email tx {:email (first args)}))))

    ((first (query-tester all_nodes)) :Admin)

    (query-tester get-all-users)

    (query-tester match-user-email {:email "jwall@braincache.io"})
    )
    
    (def user_page (query-tester match-user-has-page-email {:email "jwall@braincache.io"}))

    (get-in user_page [0 :user])



    (def jwall ((select-keys (first user_page) [:user :page]) :user))

    (def jwall-page ((select-keys (first user_page) [:user :page]) :page))

    jwall
    jwall-page

    (def another-jwall (let  [name (get-in user_page [0 :user :name])
           user (get-in user_page [0 :user])] user))
     (keys another-jwall)
    (query-tester)
    (def testers (query-tester))
    (get testers :user)
    testers
    (count testers)

    (def  jwall (get-in (get-user-by-email "jwall@braincache.io") [0 :user]))
    (select-keys jwall [:email :password])
    jwall

    (slurp "https://clojuredocs.org")

    )