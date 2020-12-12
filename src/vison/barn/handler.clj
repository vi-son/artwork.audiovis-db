(ns vison.barn.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.credentials :as mcr]
            [cheshire.core :as cc]
            [taoensso.timbre :as timbre
             :refer [log  trace  debug  info  warn  error  fatal  report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]])
  (:import [com.mongodb MongoOptions ServerAddress]))

(defn db-connection []
  (let [admin-db "admin"
        u        "***REMOVED***"
        p        "***REMOVED***"
        cred     (mcr/create u admin-db p)
        host     "127.0.0.1"]
    (mg/connect-with-credentials host cred)))

(defn get-entries []
  (let [conn (db-connection)
        db   (mg/get-db conn "harvester")
        coll "documents"]
    (map #(select-keys % [:session :mappings])
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

  (POST "/entry" req
        (let [body (cc/parse-string (slurp (:body req)) true)]
          (info "\n-----\n")
          (info (get-in req [:headers "host"]))
          (info (pr-str body))
          (info "\n-----\n")
          (if (or (= "https://harvester.mixing-senses.art" (get-in req [:headers "origin"]))
                  (= "127.0.0.1:3000" (get-in req [:headers "host"])))
            (do
              (store-entry body)
              {:status 201
               :headers {"Content-Type" "application/json"
                         "Access-Control-Allow-Origin" "http://harvester.mixing-senses.art"
                         "Access-Control-Allow-Headers" "Content-Type"}}
              :body body)
            {:status 403})))
  
  (route/not-found "Not Found"))

(def app
  (wrap-json-response app-routes))
