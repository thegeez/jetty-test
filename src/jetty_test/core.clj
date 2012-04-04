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
      (let [output-stream (.getOutputStream response)]
        (doto response
          (.setStatus 200)
          (.setHeader "Content-Type" "text/plain")
          (.setHeader "Connection" "keep-alive")
          (.setHeader "Transfer-Encoding" "chunked")
          .flushBuffer)

        (.print output-stream "Begin\n")
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
