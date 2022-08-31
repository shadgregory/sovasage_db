(ns sovasage-db.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [clojure.walk :refer [keywordize-keys]]
            [ring.util.codec :refer [form-decode]]))

(def db (atom {:x 3 :y 4 :z 5}))

(defn handler [request]
  (let [uri (:uri request)]
    (case uri
      "/" {:status 200
           :headers {"Content-Type" "text/html"}
           :body "<html><body><h2>Welcome to Sovasage DB!</h2></body></html>"}
      "/all" {:status 200
              :headers {"Content-Type" "text/html"}
              :body (str "<html><body><p>"
                         (clojure.string/join "<p>" @db)
                         "</body></html>")}
      "/get" (if (nil? (:query-string request))
               {:status 200
                :headers {"Content-Type" "text/html"}
                :body "Query string is required!"}
               (let [args (keywordize-keys (form-decode (:query-string request)))]
                 {:status 200
                  :headers {"Content-Type" "text/html"}
                  :body (str "<html><body>"
                             (get @db (keyword (:key args)) "No value found")
                             "</body></html>")}))
      "/set" (if (nil? (:query-string request))
               {:status 200
                :headers {"Content-Type" "text/html"}
                :body "Query string is required!"}
               (let [args (keywordize-keys (form-decode (:query-string request)))]
                 (doseq [[key val] args]
                   (swap! db assoc key val))
                 {:status 200
                  :headers {"Content-Type" "text/html"}
                  :body (str "<html><body>"
                             @db
                             "</body></html>")}))
      {:status 404
       :body "<h2>Page not found.</h2>"})))

(defn -main
  "I don't do a whole lot."
  [& args]
  (jetty/run-jetty
   (wrap-reload handler)
   {:port 4000
    :join? false}))
