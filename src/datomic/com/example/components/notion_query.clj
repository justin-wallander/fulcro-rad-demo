(ns com.example.components.notion-query
  (:require
   [neo4j-clj.core :as db]
   [org.httpkit.client :as http]
   [clojure.data.json :as json]
  ;;  [org.httpkit.sni-client :as sni-client]  
   [mount.core :refer [defstate]])
  (:import (java.net URI))
  )
   



  ;; Change default client for your whole application:
;; (alter-var-root #'org.httpkit.client/*default-client* (fn [_] sni-client/default-client))


  (def neo4j-gcp-bolt-db
    (db/connect (URI. "bolt://34.72.52.231:7687")
                "neo4j"
                "jwall"))

  (def neo4j-bolt-db
    (db/connect (URI. "bolt://35.237.229.3:7687")
                "jwall"
                "2688"))

  (def token "secret_Eh9LEwrFvdKJsHLygDSX7QRMCwOV8RnlK4WSKUE6NGn")
  (def notion-customer-db-id "de297bebecf84283bafa4a51b174f326")


  (def notion-customers (let [options {:headers {"Authorization" (str "Bearer " token) "Notion-Version" "2021-08-16" "Content-Type" "application/json"}}
                              {:keys [status headers body error] :as resp} @(http/post (format "https://api.notion.com/v1/databases/%s/query" notion-customer-db-id) options)]
                          (if error
                            (println "Failed, exception: " error)
                            (json/read-str body))))
;; (defn from-notion-idx-customer-email [& args] (get-in (((((get-in (first args) ["results" (first (rest args))]) "properties") "personEmail") "rollup") "array") [(first (rest args)) "email"]))
;; notion-customers
;; (def from-notion-idx-customer-email (get-in (((((get-in notion-customers ["results" 0]) "properties") "personEmail") "rollup") "array") [0 "email"]))
;; (from-notion-idx-customer-email [notion-customers 0])
;; from-notion-idx-customer-email


;; (keys ((get-in notion-customers ["results" 0]) "properties"))


  (db/defquery match-user-email
    "MATCH (u:User {email: $email}) RETURN u as user")

  (db/defquery all_nodes
    "Match (n)  return n{.*} as Nodes, labels(n) as Label")

  (db/defquery count-nodes
    "Match (n) return count(n)")


  (db/defquery type-nodes
    "Match (n)  return distinct labels(n)")

  (db/defquery get-specific-nodes
    "MATCH (n) WHERE $node IN labels(n) RETURN n as node")

  (db/defquery missing-nodes
    "Match (n) where labels(n) is null return n, id(n)")

  (db/defquery user-nodes
    "MATCH (u:User) RETURN u as user")

  (db/defquery get-activity<-user-nodes
    "MATCH (n:Activity)<-[]-(u)  where u.email is not null and (not (u.email contains 'jwall@')) RETURN n as activity, u as user")

  (db/defquery get-investor-nodes
    "MATCH (n:Investor) RETURN n as investor")
  
  (db/defquery updating-users-v1
    "WITH $results as results
  FOREACH(row in results | MERGE (u:User{email: row['user']['email']}) SET u = row['user']) return count(*)")

 (db/defquery updating-Page-v1
   "WITH $results as results
  FOREACH(row in results | MERGE (p:Page {ep: row['page']['ep']}) SET p = row['page']) return count(*)")

 (db/defquery updating-Proposal-v1
   "WITH $results as results
  FOREACH(row in results | MERGE (p:Proposal {tfToken: row['proposal']['tfToken']}) SET p = row['proposal']) return count(*)")


  ;; updating-Investor-v1
  ;; updating-LoginActivity-v1
  


  (db/defquery updating-users-pages
    "WITH $results as results
  FOREACH(row in results |
  MERGE (u:User{email: row['properties']['personEmail']['rollup']['array'][0]['email']})
  SET u.ep= row['properties']['ep']['title'][0]['plain_text'],
  u.pageID= row['properties']['personName']['relation'][0]['id'],
  u.name= row['properties']['name']['formula']['string'],
  u.oppsID= row['properties']['opps']['relation'][0]['id'],
  u.pin= row['properties']['pin']['rich_text'][0]['plain_text']
  MERGE (p:Page{ep: row['properties']['ep']['title'][0]['plain_text']})
  ON CREATE SET p.uploads = []
  SET p.heroMedia= row['properties']['heroMedia']['url'],
  p.noteMedia= row['properties']['noteMedia']['url'],
  p.aboutMedia= row['properties']['aboutMedia']['url'],
  p.hubDescription= row['properties']['hubDescription']['rich_text'][0]['plain_text'],
  p.projectDevelopment= row['properties']['*projectDevelopment']['rich_text'][0]['plain_text'],
  p.announcements= row['properties']['*announcements']['rich_text'][0]['plain_text'],
  p.projectProspect= row['properties']['*projectProspect']['rich_text'][0]['plain_text'],
  p.projectDelivered= row['properties']['*projectDelivered']['rich_text'][0]['plain_text'],
  p.customNote= row['properties']['customNote']['rich_text'][0]['plain_text'],
  p.houseCount= row['properties']['houseCount']['rich_text'][0]['plain_text'],
  p.scopeSummary= row['properties']['scopeSummary']['rich_text'][0]['plain_text'],
  p.proposalSubmitDate= row['properties']['_proposalSubmitDate']['formula']['string'],
  p.elevationCount = row['properties']['elevationCount']['rich_text'][0]['plain_text'],
  p.proposalSignedDate= row['properties']['proposalSignedDate']['rollup']['array'][0]['date'],
  p.stage= row['properties']['stage']['rollup']['array'][0]['select']['name'],
  p.deliverables= row['properties']['deliverables']['rich_text'][0]['plain_text'],
  p.clientReqs= row['properties']['clientReqs']['rich_text'][0]['plain_text'],
  p.proposalAmount = row['properties']['proposalAmount']['number'],
  p.hubMedia = row['properties']['hubMedia']['url'],
  p.greetingName = row['properties']['greetingName']['rich_text'][0]['plain_text']

  MERGE (u)-[:HAS_PAGE]->(p))
  return count(*)")



  
  
  (defn gcp-neo4j-query
    [query & args]
    (db/with-transaction neo4j-gcp--bolt-db tx
      (vec (query tx (first args)))))
  





;; (db/defquery updating-users-v3
  ;; "WITH $results as results
  ;; FOREACH(row in results | MERGE (p:Proposal {tfToken: row['proposal']['tfToken']}) SET p = row['proposal']) return count(*)")


  ;;  (def new-activity (query-tester get-Nodes))

  ;;  (def old-activity (old-query-tester get-Nodes))
  ;; user specific 

  ;; (count old-activity)
  ;; old-activity

  ;;  (def new-proposal (query-tester get-Nodes))

  ;; (def old-proposal (old-query-tester get-Nodes))
  ;; (count new-proposal)


  ;; (query-tester updating-users-v3 {:results old-proposal})


;; (for [user users]
  ;; ((user :user) :email))

(comment
  (def new-pages (query-tester get-Nodes))
  (count new-pages)
  (count old-pages)

  (query-tester updating-users-v2 {:results old-pages})



  (def old-pages (old-query-tester get-Nodes))

  (for [page old-pages]
    ((page :page) :ep))


  (old-query-tester count-nodes)

  (defn old-query-tester
    [query & args]
    (db/with-transaction neo4j-bolt-db tx
      (vec (query tx (first args)))))

  (old-query-tester get-specific-nodes {:node "LoginActivity"})


  (old-query-tester get-investor-nodes)
  (type users)
  (def new-users (query-tester user-nodes))
  (def old-users (old-query-tester user-nodes))
  (count new-users)
  new-users




  (query-tester updating-users-v1 {:results old-users})

  (for [user users]
    (user :user))

  (let [(for [user users]
          (assoc users1 user))] users1)

  (let [user [:user] (old-query-tester user-nodes)] user)

  (query-tester match-user-email {:email "jwall@braincache.io"})

  ((first (query-tester all_nodes)) :Admin)
  (old-query-tester all_nodes)
  (def old-nodes (old-query-tester all_nodes))

  ((nth old-nodes 0) :Label)

  old-nodes


  (select-keys old-nodes [:Nodes])

  (query-tester updating-users-pages {:results results})

  (query-tester count_nodes))


(comment
  
    comps = '''CALL apoc.periodic.iterate('MATCH (u:User) RETURN u, apoc.static.getAll("notion") as notion',
  'CALL apoc.load.jsonParams(notion.pageUrl + u.pageID,
  {Authorization: notion.token,`Notion-Version`: "2021-08-16"}, null) yield value 
  with u, value 
  set u.company = value["properties"]["comp"]["formula"]["string"]',{})'''

  opps = '''CALL apoc.periodic.iterate('MATCH (u:User) return u, apoc.static.getAll(\\'notion\\') as notion',
  'CALL apoc.load.jsonParams(notion.pageUrl + u.oppsID,
  {Authorization: notion.token,`Notion-Version`: \\'2021-08-16\\'},null)
  yield value
  with u, value
  set u.opps = value[\\'properties\\'][\\'Property Name\\'][\\'title\\'][0][\\'plain_text\\']',{})'''

  users_pages = '''WITH apoc.static.getAll('notion') as notion
  CALL apoc.load.jsonParams(notion.dbUrl + 'de297bebecf84283bafa4a51b174f326' + '/query',
  {Authorization: 'Bearer ' + notion.token,`Notion-Version`: '2021-08-16'},'',null,{method:'POST'})
  YIELD value
  WITH value.results as results
  FOREACH(row in results |
  MERGE (u:User{email: row['properties']['personEmail']['rollup']['array'][0]['email']})
  SET u.ep= row['properties']['ep']['title'][0]['plain_text'],
  u.pageID= row['properties']['personName']['relation'][0]['id'],
  u.name= row['properties']['name']['formula']['string'],
  u.oppsID= row['properties']['opps']['relation'][0]['id'],
  u.pin= row['properties']['pin']['rich_text'][0]['plain_text']
  MERGE (p:Page{ep: row['properties']['ep']['title'][0]['plain_text']})
  ON CREATE SET p.uploads = []
  SET p.heroMedia= row['properties']['heroMedia']['url'],
  p.noteMedia= row['properties']['noteMedia']['url'],
  p.aboutMedia= row['properties']['aboutMedia']['url'],
  p.hubDescription= row['properties']['hubDescription']['rich_text'][0]['plain_text'],
  p.projectDevelopment= row['properties']['*projectDevelopment']['rich_text'][0]['plain_text'],
  p.announcements= row['properties']['*announcements']['rich_text'][0]['plain_text'],
  p.projectProspect= row['properties']['*projectProspect']['rich_text'][0]['plain_text'],
  p.projectDelivered= row['properties']['*projectDelivered']['rich_text'][0]['plain_text'],
  p.customNote= row['properties']['customNote']['rich_text'][0]['plain_text'],
  p.houseCount= row['properties']['houseCount']['rich_text'][0]['plain_text'],
  p.scopeSummary= row['properties']['scopeSummary']['rich_text'][0]['plain_text'],
  p.proposalSubmitDate= row['properties']['_proposalSubmitDate']['formula']['string'],
  p.elevationCount = row['properties']['elevationCount']['rich_text'][0]['plain_text'],
  p.proposalSignedDate= row['properties']['proposalSignedDate']['rollup']['array'][0]['date'],
  p.stage= row['properties']['stage']['rollup']['array'][0]['select']['name'],
  p.deliverables= row['properties']['deliverables']['rich_text'][0]['plain_text'],
  p.clientReqs= row['properties']['clientReqs']['rich_text'][0]['plain_text'],
  p.proposalAmount = row['properties']['proposalAmount']['number'],
  p.hubMedia = row['properties']['hubMedia']['url'],
  p.greetingName = row['properties']['greetingName']['rich_text'][0]['plain_text']

  MERGE (u)-[:HAS_PAGE]->(p))
  return count(*)''')