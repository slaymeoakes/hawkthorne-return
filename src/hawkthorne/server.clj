(ns hawkthorne.server
  "Core server components to serve web pages"
  (:gen-class)
  (:require [hawkthorne.middleware :as middleware]
            [hawkthorne.router :as router]
            [hawkthorne.websocket :as websocket]
            [org.httpkit.server :as http]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.not-modified :as not-modified]))

(defonce server (atom nil))

(def app
  "Application route handling, authentication and middleware"
  (-> router/route-handler
      (defaults/wrap-defaults (assoc-in defaults/site-defaults
                                        [:security :anti-forgery] false))
      middleware/wrap-transit-params
      middleware/wrap-transit-response
      not-modified/wrap-not-modified))

(defn stop
  "Stop the Web server"
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil))
  (websocket/tick-stop)
  (println "Stopped."))

(defn start
  "Start Web server and initialize stylsheet and DB"
  []
  (when (nil? @server)
    (reset! server (http/run-server #'app {:port 8080})))
  (websocket/tick-start)
  (println "Started."))

(defn restart [] (stop) (start))

(defn -main [] (start))
