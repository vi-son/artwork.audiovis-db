(ns vi.son-harvester-backend.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.credentials :as mcr]
            [monger.json :as mj]
            [cheshire.core :as cc]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]])
  (:import [com.mongodb MongoOptions ServerAddress]))

(defn insert []
  (let [conn (mg/connect)
        db   (mg/get-db conn "harvester")
        coll "documents"]
    (mc/insert db coll {:foo "bar"})))

(defn get-entries []
  (let [conn (mg/connect)
        db   (mg/get-db conn "harvester")
        coll "documents"]
    (mc/find-maps db "documents")))

(defn store-entry [e]
  (let [conn (mg/connect)
        db   (mg/get-db conn "harvester")
        coll "documents"]
    (mc/insert db coll e)
    e))

(defroutes app-routes
  (GET "/" [] {:status 200
               :headers {"Content-Type" "application/json"}
               :body {:message "OK"}})

  (GET "/entries" [] {:status 200
                      :headers {"Content-Type" "application/json"}
                      :body (get-entries)})

  (POST "/entry" req
        (let [body (cc/parse-string (slurp (:body req)) true)]
          (store-entry body)
          {:status 201
           :headers {"Content-Type" "application/json"
                     "Access-Control-Allow-Origin" "*"
                     "Access-Control-Allow-Headers" "Content-Type"}
           :body body}))
  
  (route/not-found "Not Found"))

(def app
  (wrap-json-response app-routes))
