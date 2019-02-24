(ns oubiwann.blog.reader
  (:require
    [clojure.data.xml :as xml]
    [clojusc.twig :refer [pprint]]
    [dragon.components.config :as config]
    [dragon.components.db :as db-component]
    [dragon.data.sources.core :as db]
    [taoensso.timbre :as log]))

(defn atom-entry
  [system post]
  (let [title (get-in post [:metadata :title])
        uri-posts (config/posts-path system)
        uri (str uri-posts (:uri-path post))]
    [:entry
     [:title title]
     [:updated (get-in post [:dates :timestamp])]
     [:author [:name (get-in post [:metadata :author])]]
     [:link {:href (format "http://%s/%s" (config/domain system) uri)}]
     [:id (format "%s:feed:post:%s"
                  (config/domain-urn system)
                  title)]
     [:content {:type "html"} (:content post)]]))

(defn atom-feed
  [system route]
  (let [querier (db-component/db-querier system)
        post-keys (db/get-last-n-keys querier
                                      (config/feed-count system))
        posts (map #(db/get-all-data querier %) post-keys)]
    (xml/emit-str
     (xml/sexp-as-element
      [:feed {:xmlns "http://www.w3.org/2005/Atom"}
       [:id (format "%s:feed" (config/domain-urn system))]
       [:updated (-> posts first (get-in [:dates :timestamp]))]
       [:title {:type "text"} (config/name system)]
       [:link {:rel "self" :href (format "http://%s%s" (config/domain system) route)}]
       (map #(atom-entry system %) posts)]))))
