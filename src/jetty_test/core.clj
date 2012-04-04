(ns jetty-test.core
  (:require [ring.util.servlet :as servlet])
  (:import (org.eclipse.jetty.server.handler AbstractHandler)
           (org.eclipse.jetty.server Server Request Response)
           (org.eclipse.jetty.server.bio SocketConnector)
           (javax.servlet.http HttpServletRequest)))


(defn- proxy-handler
  "Returns an Jetty Handler implementation for the given Ring handler."
  []
  (proxy [AbstractHandler] []
    (handle [target ^Request base-request ^HttpServletRequest request response]
      (let [request-map (servlet/build-request-map request)
            output-stream (.getOutputStream response)]
        (servlet/set-status response 200)
        (servlet/set-headers response {"Content-Type" "text/plain"
                                       "Cache-Control" "no-cache, no-store, max-age=0, must-revalidate"
                                       "Pragma" "no-cache"
                                       "Expires" "Fri, 01 Jan 1990 00:00:00 GMT"
                                       "X-Content-Type-Options" "nosniff"
                                       "Transfer-Encoding" "chunked"})
        (.flushBuffer response)
        (try
          (dotimes [i 50]
            (doto output-stream
              (.print "Hello")
              (.println (str i))
              (.flush))
            (Thread/sleep 1000))
          (doto output-stream
            (.print "Done")
            (.flush)
            (.close))
          (catch Exception e
            (println "CONNECTION LOST!")))
        (.setHandled base-request true)))))


(defn- create-server
  "Construct a Jetty Server instance."
  [options]
  (let [connector (doto (SocketConnector.)
                    (.setPort (options :port 80))
                    (.setHost (options :host)))
        server    (doto (Server.)
                    (.addConnector connector)
                    (.setSendDateHeader true))]
    server))

(defn ^Server run-jetty
  [options]
  (doto (create-server options)
    (.setHandler (proxy-handler))
    (.start)))

(defn -main
  [& args]
  (run-jetty {:port (Integer.
                     (or
                      (System/getenv "PORT")
                      8080))
              :join? false}))

(comment
  (do
    (.stop server)
    (def server (-main))
    )
  )
