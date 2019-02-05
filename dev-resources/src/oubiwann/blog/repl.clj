(ns oubiwann.blog.repl
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
    [clojusc.blogger.xml.parser.export :as blogger]
    [clojusc.system-manager.core :refer :all]
    [clojusc.twig :as logger]
    [dragon.blog.core :as blog]
    [dragon.blog.generator :as gen]
    [dragon.blog.post.core :as post]
    [dragon.config :as dragon-config]
    [dragon.util :as util]
    [expound.alpha :as expound]
    [inspectable.repl :refer [why browse-spec]]
    [oubiwann.blog.cli.core :as cli]
    [oubiwann.blog.components.core]
    [oubiwann.blog.core :as core]
    [oubiwann.blog.email.content :as email-content]
    [oubiwann.blog.email.delivery :as email-delivery]
    [oubiwann.blog.import :as ob-import]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Initial Setup & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def setup-options {
  :init 'oubiwann.blog.components.core/init
  :after-refresh 'oubiwann.blog.repl/init-and-startup
  :throw-errors false})

(defn init
  "This is used to set the options and any other global data.

  This is defined in a function for re-use. For instance, when a REPL is
  reloaded, the options will be lost and need to be re-applied."
  []
  (logger/set-level! '[oubiwann.blog dragon] :debug)
  (setup-manager setup-options))

(defn init-and-startup
  "This is used as the 'after-refresh' function by the REPL tools library.
  Not only do the options (and other global operations) need to be re-applied,
  the system also needs to be started up, once these options have be set up."
  []
  (init)
  (startup))

;; It is not always desired that a system be started up upon REPL loading.
;; Thus, we set the options and perform any global operations with init,
;; and let the user determine when then want to bring up (a potentially
;; computationally intensive) system.
(init)

(defn banner
  []
  (println (slurp (io/resource "text/banner.txt")))
  :ok)

; (defn show-lines-with-error
;   "Process posts and show the lines of text that threw exceptions."
;   []
;   (->> (blog/get-posts)
;        (map #(->> %
;                  (post/add-post-data)
;                  :text))
;        (pprint)))
