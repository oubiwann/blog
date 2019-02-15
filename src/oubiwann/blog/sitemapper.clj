(ns oubiwann.blog.sitemapper
  (:require
    [clojure.data.xml :as xml]
    [clojusc.twig :refer [pprint]]
    [dragon.util :as util]
    [taoensso.timbre :as log]))

(defn url
  [datestamp route]
  [:url
   [:loc (str "http://oubiwann.github.io" route)]
   [:lastmod datestamp]
   [:changefreq "weekly"]])

(defn urlset
  [datestamp routes]
  [:urlset {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"}
   (map (partial url datestamp) (keys routes))])

(defn generate
  [routes]
  (let [datestamp (util/format-datestamp (util/now :datetime-map))]
    (xml/emit-str
     (xml/sexp-as-element
      (urlset datestamp routes)))))
