(ns vison.audiovisio.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [monger.core :as mg]
            [monger.collection :as mc]
            [edn-config.core :as cfg]
            [monger.credentials :as mcr]
            [monger.conversion :refer [from-db-object]]
            [cheshire.core :as cc]
            [taoensso.timbre :as timbre
             :refer [log  trace  debug  info  warn  error  fatal  report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]])
  (:import [com.mongodb MongoOptions ServerAddress]))

(def CONFIG (atom (cfg/load-file "./configuration.edn")))

(defn db-connection []
  (info "\n\n---- CONFIG:" (pr-str @CONFIG) "\n\n")
  (let [admin-db "admin"
        u        (:db-user (:database @CONFIG))
        p        (:db-password (:database @CONFIG))
        cred     (mcr/create u admin-db p)
        host     "127.0.0.1"]
    (mg/connect-with-credentials host cred)))

(defn get-entry [totem-id]
  (let [conn (db-connection)
        db   (mg/get-db conn "harvester")
        coll "documents"]
    (map #(select-keys % [:totem :date :mappings])
         (mc/find-maps db coll {:totem totem-id}))))

(defn get-entries []
  (let [conn (db-connection)
        db   (mg/get-db conn "harvester")
        coll "documents"]
    (map #(select-keys % [:totem :date :mappings])
         (mc/find-maps db "documents"))))

(defn store-entry [e]
  (let [conn (db-connection)
        db   (mg/get-db conn "harvester")
        coll "documents"]
    (mc/insert db coll e)
    e))

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body {:message "vi.son audiovis i/o API"
               :version (System/getProperty "vi.son-audiovisio-db.version") 
               :entries (count (get-entries))
               :routes {:index "GET /"
                        :entries "GET /entries"
                        :upload "POST /entry"}}})

  (GET "/entries" []
       (let [entries (get-entries)]
         (info (pr-str entries))
         {:status 200
          :headers {"Content-Type" "application/json"}
          :body entries}))

  (GET "/entry/:id" [id]
       {:status 200
        :body {:totem (get-entry id)}})

  (POST "/entry" req
        (let [body (cc/parse-string (slurp (:body req)) true)]
          (info "\n-----\n")
          (info (get-in req [:headers "host"]))
          (info (pr-str body))
          (info "\n-----\n")
          (if (or (= "https://audiovisio.mixing-senses.art" (get-in req [:headers "origin"]))
                  (= "127.0.0.1:3000" (get-in req [:headers "host"]))
                  (= "127.0.0.1:3333" (get-in req [:headers "host"])))
            (do
              (store-entry body)
              {:status 201
               :headers {"Content-Type" "application/json"
                         "Access-Control-Allow-Origin" "http://harvester.mixing-senses.art"
                         "Access-Control-Allow-Headers" "Content-Type"}
               :body {:totem (:totem body)}})
            {:status 403})))
  
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-cors 
       :access-control-allow-origin [#".*"]
       :access-control-allow-methods [:get :post])))
