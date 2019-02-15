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

(defn process-files
  [system]
  (blog/process system))

(defn generate
  [system]
  (let [generated-routes (routes/all system)]
    (generator/run system generated-routes)
    ; (email-content/gen system)
    ; (social-content/gen system)
    ))
