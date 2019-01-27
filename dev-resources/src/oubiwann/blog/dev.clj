(ns oubiwann.blog.dev
  "Blog development namespace

  This namespace is particularly useful when doing active development on the
  blog application."
  (:require
    [clojure.math.combinatorics :refer [cartesian-product]]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint print-table]]
    [clojure.reflect :refer [reflect]]
    [clojure.spec.alpha :as spec]
    [clojure.string :as string]
    [clojure.tools.namespace.repl :as repl]
    [clojure.walk :refer [macroexpand-all]]
    [clojusc.twig :as logger]
    [dragon.blog.core :as blog]
    [dragon.blog.generator :as gen]
    [dragon.blog.post.core :as post]
    [dragon.config.core :as config]
    [dragon.util :as util]
    [expound.alpha :as expound]
    [inspectable.repl :refer [why browse-spec]]
    [oubiwann.blog.cli.core :as cli]
    [oubiwann.blog.components.system :as system]
    [oubiwann.blog.core :as core]
    [oubiwann.blog.email.content :as email-content]
    [oubiwann.blog.email.delivery :as email-delivery]
    [oubiwann.blog.main :as main]
    [oubiwann.blog.reader :as reader]
    [oubiwann.blog.routes :as routes]
    [oubiwann.blog.social.content :as social-content]
    [oubiwann.blog.social.google-plus :as gplus]
    [oubiwann.blog.social.twitter :as twitter]
    [oubiwann.blog.util :as ob-util]
    [oubiwann.blog.web.content.data :as data]
    [oubiwann.blog.web.content.page :as page]
    [selmer.parser :as selmer]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]
    [trifl.java :refer [show-methods]]))

(logger/set-level! '[oubiwann.blog dragon] :debug)

(defn start-system
  []
  (system/start))

;; Aliases

(def reload #'repl/refresh)
(def reset #'repl/refresh)

; (defn show-lines-with-error
;   "Process posts and show the lines of text that threw exceptions."
;   []
;   (->> (blog/get-posts)
;        (map #(->> %
;                  (post/add-post-data)
;                  :text))
;        (pprint)))
