(ns oubiwann.blog.components.core
  (:require [com.stuartsierra.component :as component]
            [dragon.components.core :as dragon-components]
            [oubiwann.blog.config :as config-lib]))

(defn initialize-with-web
  [cfg-data]
  (component/map->SystemMap
    (merge (dragon-components/cfg cfg-data)
           dragon-components/log
           dragon-components/redis
           dragon-components/data
           dragon-components/evt
           dragon-components/http
           dragon-components/wtchr
           (dragon-components/rspndr))))

(def init-lookup
  {:default #'dragon-components/initialize-default
   :basic #'dragon-components/initialize-bare-bones
   :web #'initialize-with-web})

(defn init
  ([]
    (init :web))
  ([mode]
    (init mode (config-lib/data)))
  ([mode cfg-data]
    ((mode init-lookup) cfg-data)))
