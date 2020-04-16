(defproject vi.son-barn "0.0.1-SNAPSHOT"
  :description ""
  :url "https://barn.mixing-senses.art"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.novemberain/monger "3.1.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler vison.barn.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
