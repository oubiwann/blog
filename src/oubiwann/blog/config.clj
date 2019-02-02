(ns oubiwann.blog.config
  (:require
    [clojusc.config.unified.config :as config]
    [dragon.config :as dragon-config]
    [dragon.util :as util])
  (:refer-clojure :exclude [name read]))

(def config-file "config/oubiwann-blog/config.edn")

(defn data
  ([]
    (data config-file))
  ([filename]
    (util/deep-merge
    	(dragon-config/data)
    	(config/data filename))))
