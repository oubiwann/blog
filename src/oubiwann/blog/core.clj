(ns oubiwann.blog.core
  (:require [clojusc.twig :as logger]
            [dragon.blog.core :as blog]
            [dragon.blog.generator :as generator]
            [oubiwann.blog.util :as util]
            [oubiwann.blog.email.content :as email-content]
            [oubiwann.blog.routes :as routes]
            [oubiwann.blog.social.content :as social-content]
            [trifl.docs :as docs]))

(def version util/version)

(defn process-all
  [system]
  (blog/process system))

(defn process-file
  [system src-file]
  (blog/process-file system src-file))

(defn process-route
  [system route]
  (when (routes/post-route? system route)
    (process-file system (routes/route->file system route))))

(defn generate
  ([system]
    (let [generated-routes (routes/all system)]
      (generator/run system generated-routes)
      ; (email-content/gen system)
      ; (social-content/gen system)
      ))
  ([system route]
    (let [generated-routes (routes/one system route)]
      (generator/run system generated-routes))))

(defn run
  ([system]
    (process-all system)
    (generate system))
  ([system route]
    (process-route system route)
    (generate system route)))
