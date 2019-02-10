(ns oubiwann.blog.email.delivery
  (:require
    [clojure.java.io :as io]
    [oubiwann.blog.email.content :as email-content]
    [oubiwann.blog.util :as util]
    [postal.core :as postal]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]))

(def to "oubiwann :: blog | New Post Announcements <oubiwann-blog-new-posts@googlegroups.com>")
(def from "oubiwann :: blog | Updates <oubiwann-blog-updates@googlegroups.com>")

(defn make-message
  [to from subject content]
  {:from from
   :to to
   :subject subject
   :body [{:type "text/html; charset=utf-8"
           :content content}]})

(defn log-delivery-status
  [result]
  (log/infof "Message delivery status: %s - %s"
             (name (:error result))
             (:message result)))

(defn send-message
  [subject content]
  (log/infof "Sending message '%s' to %s ..." subject to)
  (->> content
       (make-message to from subject)
       (postal/send-message)
       (log-delivery-status))
  :ok)

(defn send-new-post-message
  [post-file]
  (let [post-data (util/get-post-data post-file)
        msg-file (util/get-message-content-file
                   post-file email-content/new-post-file)
        msg-content (slurp msg-file)]
    (send-message (:title post-data) msg-content)))
