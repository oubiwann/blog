(ns oubiwann.blog.dev
  "FRMX Blog development namespace

  This namespace is particularly useful when doing active development on the
  FRMX Blog application."
  (:require
    [clojure.math.combinatorics :refer [cartesian-product]]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint print-table]]
    [clojure.reflect :refer [reflect]]
    [clojure.string :as string]
    [clojure.tools.namespace.repl :as repl]
    [clojure.walk :refer [macroexpand-all]]
    [clojusc.twig :as logger]
    [dragon.blog.core :as blog]
    [dragon.blog.generator :as gen]
    [dragon.blog.post :as post]
    [dragon.config :as config]
    [dragon.util :as util]
    [dragon.web.core :as web]
    [oubiwann.blog.cli.core :as cli]
    [oubiwann.blog.components.system :as system]
    [oubiwann.blog.core :as core]
    [oubiwann.blog.email.content :as email-content]
    [oubiwann.blog.email.delivery :as email-delivery]
    [oubiwann.blog.main :as main]
    [oubiwann.blog.maps :as maps]
    [oubiwann.blog.reader :as reader]
    [oubiwann.blog.routes :as routes]
    [oubiwann.blog.social.content :as social-content]
    [oubiwann.blog.social.google-plus :as gplus]
    [oubiwann.blog.social.twitter :as twitter]
    [oubiwann.blog.util :as mx-util]
    [oubiwann.blog.web.content.data :as data]
    [oubiwann.blog.web.content.page :as page]
    [selmer.parser :as selmer]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]
    [trifl.java :refer [show-methods]]))

(logger/set-level! ['oubiwann.blog 'dragon] :info)

(defn start-system
  []
  (system/start (system/initialize)))

;;; Aliases

(def reload #'repl/refresh)
(def reset #'repl/refresh)

(defn show-lines-with-error
  "Process posts and show the lines of text that threw exceptions."
  []
  (->> (blog/get-posts)
       (map #(->> %
                 (post/add-post-data)
                 :text))
       (pprint)))
