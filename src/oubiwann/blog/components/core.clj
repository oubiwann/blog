(ns oubiwann.blog.components.core
  (:require [com.stuartsierra.component :as component]
            [dragon.components.system :as system]
            [dragon.config.core :as config-lib]))

(defn init
  ([]
    (init :web))
  ([mode]
    (init mode #'config-lib/build))
  ([mode cfg-builder-fn]
    ((mode system/init-lookup) cfg-builder-fn)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Management Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stop
  ([system]
   (component/stop system))
  ([system component-key]
   (->> system
        (component-key)
        (component/stop)
        (assoc system component-key))))

(defn start
  ([]
   (start (init)))
  ([system]
   (component/start system))
  ([system component-key]
   (->> system
        (component-key)
        (component/start)
        (assoc system component-key))))

(defn restart
  ([system]
   (-> system
       (stop)
       (start)))
  ([system component-key]
   (-> system
       (stop component-key)
       (start component-key))))
