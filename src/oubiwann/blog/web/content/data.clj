(ns oubiwann.blog.web.content.data
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.core :as blog]
    [dragon.blog.tags :as tags]
    [dragon.data.sources.core :as db]
    [dragon.components.config :as config]
    [dragon.components.db :as db-component]
    [markdown.core :as markdown]
    [oubiwann.blog.util :as util]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Helper Functions & Data Helpers   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def legal-block-extensions
  #{".selmer-block"})

(def legal-block-names
  #{"pre-css"
    "pre-head-scripts"
    "post-head-scripts"
    "head-postpends"
    "post-post-scripts"
    "article-body-ads"
    "article-sidebar-comments-links"})

(defn legal-block-file?
  [^java.io.File file]
  (->> legal-block-extensions
       (map #(string/ends-with? (.getCanonicalPath file) %))
       (remove false?)
       (not-empty)))

(defn legal-block-name?
  [block-name]
  (contains? legal-block-names block-name))

(defn block-matches?
  [block-file block-name]
  (and (legal-block-name? block-name)
       (string/includes? (str block-file) block-name)))

(defn get-block-name
  "Given a file object, "
  [^java.io.File block-file]
  (->> legal-block-names
       (filter (partial block-matches? block-file))
       (first)))

(defn get-block-files
  [parent-dir]
  (->> parent-dir
       (io/file)
       (.listFiles)
       (filter legal-block-file?)))

(defn get-block
  [block-file]
  (let [block-name (get-block-name block-file)]
    (if (nil? block-name)
      []
      [(keyword block-name) (slurp block-file)])))

(defn get-blocks
  [post-data]
  (->> post-data
       :src-dir
       (get-block-files)
       (map get-block)
       (into {})))

(defn posts-stats
  [posts]
  {:posts (count posts)
   :authors (->> posts
                 (map :author)
                 set
                 count)
   :lines (->> posts
               (map :line-count)
               (reduce +))
   :words (->> posts
               (map :word-count)
               (reduce +))
   :chars (->> posts
               (map :char-count)
               (reduce +))})

(defn base
  [system]
  {:page-data {
     :base-path (config/base-path system)
     :site-title (config/name system)
     :site-description (config/description system)
     :site-generator (util/version)
     :index "index"
     :about "about"
     :archives "archives"
     :categories "categories"
     :tags "tags"
     :authors "authors"
     :active nil}})

(defn common
  [system]
  (assoc (base system)
         :blog-stats (db/get-all-stats (db-component/db-querier system))))

(def generic-page
  {:title nil
   :subtitle nil})

(defn markdown-page
  [md-file]
  (merge
    generic-page
    {:body (->> md-file
                (str "markdown/")
                (io/resource)
                (slurp)
                (markdown/md-to-html-string))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Static Pages Data   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn about
  [system]
  (-> system
      common
      (assoc-in [:page-data :active] "about")
      (assoc :content (-> "about.md"
                          (markdown-page)
                          (assoc :title "About")))))

(defn contact
  [system]
  (-> system
      common
      (assoc-in [:page-data :active] "about")
      (assoc :content (-> "contact.md"
                          (markdown-page)
                          (assoc :title "Contact Us")))))

(defn powered-by
  [system]
  (-> system
      common
      (assoc-in [:page-data :active] "about")
      (assoc :content (-> "powered-by.md"
                          (markdown-page)
                          (assoc :title "Powered By")))))

(defn license
  [system]
  (-> system
      common
      (assoc-in [:page-data :active] "about")
      (assoc :content (-> "license.md"
                          (markdown-page)
                          (assoc :title "Content License")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dynamic Pages Data   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post
  [system posts post-data]
  (-> system
      common
      (assoc-in [:page-data :active] "archives")
      (assoc :post-data post-data
             :blocks (get-blocks post-data)
             :tags (tags/unique post-data))))

(defn front-page
  [system all-posts top-posts & {:keys [above-fold-count below-fold-count column-count]}]
  (let [above-posts (take above-fold-count top-posts)
        headliner (first above-posts)
        grouped-posts (partition column-count
                                 (nthrest above-posts 1))
        below-posts (nthrest top-posts above-fold-count)]
    (-> system
        (common all-posts)
        (assoc-in [:page-data :active] "index")
        (assoc :headlines-heading "Headlines"
               :headlines-desc (str "We like to keep things simple at FRMX. "
                                    "Only the most recent headlines are kept "
                                    "on the front page -- if you want to read "
                                    "an older post, <a href=\"/blog/archives\""
                                    ">check out the archives</a>.")
               :tags (tags/get-stats all-posts)
               :headliner headliner
               :posts-data grouped-posts
               :posts-count (count top-posts)
               :above-count (count above-posts)
               :below-count (count below-posts)
               :below-fold-data below-posts))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Listings Pages   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn archives
  [system posts]
  (-> system
      common
      (assoc-in [:page-data :active] "archives")
      (assoc :content (assoc generic-page :title "Archives")
             :posts-data (blog/group-data :archives posts))))

(defn categories
  [system posts]
  (-> system
      common
      (assoc-in [:page-data :active] "categories")
      (assoc :content (assoc generic-page :title "Categories")
             :posts-data (blog/group-data :categories posts))))

(defn tags
  [system posts]
  (-> system
      common
      (assoc-in [:page-data :active] "tags")
      (assoc :content (assoc generic-page :title "Tags")
             :posts-data (blog/group-data :tags posts))))

(defn authors
  [system posts]
  (-> system
      common
      (assoc-in [:page-data :active] "authors")
      (assoc :content (assoc generic-page :title "Authors")
             :posts-data (blog/group-data :authors posts))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Design Pages Data   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn design
  [system posts]
  (-> system
      common
      (assoc-in [:page-data :active] "design")
      (assoc :content (assoc generic-page :title "Design"))))
