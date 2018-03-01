(defproject server-example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.6.3"]]
  :plugins [[lein-ring "0.12.3"]]
  :ring {:handler server-example.core/full-handler
         :init    server-example.core/on-init
         :destroy server-example.core/on-destroy
         :port    3001})
