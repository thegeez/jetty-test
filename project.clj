(defproject jetty-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-servlet "1.1.0-beta2" :exclusions [javax.servlet/servlet-api]]
                 [org.eclipse.jetty/jetty-server "8.1.2.v20120308"]])
