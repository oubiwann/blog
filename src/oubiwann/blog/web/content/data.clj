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
  (let [querier (db-component/db-querier system)]
    (assoc (base system)
           :blog-stats (db/get-all-stats querier))))

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

(defn listing-data
  "Listing pages need the following:

  * year, month, day (get-post-dates)
  * post URL (get-url-path)
  * post title (get-post-metadata)
  * post subtitle (get-post-metadata)"
  [querier post-key]
  (let [dates (db/get-post-dates querier post-key)
        metadata (db/get-post-metadata querier post-key)]
    {:month-name (:month dates)
     :year (get-in dates [:date :year])
     :month (get-in dates [:date :month])
     :day (get-in dates [:date :day])
     :url-path (format "/blog/archives/%s"
                       (db/get-post-uri-path querier post-key))
     :subtitle (:subtitle metadata)
     :title (:title metadata)}))

(defn desc-str
  [a b]
  (compare b a))

(defn simple-grouping
  [querier data-fn grouping-key]
  [grouping-key (->> grouping-key
                     (data-fn querier)
                     (map #(listing-data querier %)))])

(defn two-layer-grouping
  [querier data-fn grouping-key second-level-key]
  [grouping-key (->> grouping-key
                     (data-fn querier)
                     (map #(listing-data querier %))
                     (group-by second-level-key)
                     sort
                     (into (sorted-map)))])

(defn reversed-two-layer-grouping
  [querier data-fn grouping-key second-level-key]
  [grouping-key (->> grouping-key
                     (data-fn querier)
                     (map #(listing-data querier %))
                     (group-by second-level-key)
                     sort
                     (into (sorted-map-by desc-str)))])

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

(defn code-highlight
  [system]
  (-> system
      common
      (assoc-in [:page-data :active] "design")
      (assoc :content (-> "design/code-samples.md"
                          (markdown-page)
                          (assoc :title "Code Highlight Samples")))))

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
               :headlines-desc (str "[ XXX add note about the headlines ]")
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
  "Get all years, get the post-keys for each year, then query for the
  required data for each post-key (done by the `listing-data` function)."
  [system]
  (log/info "Assembling data for archives listing page ...")
  (let [page-data (common system)
        querier (db-component/db-querier system)
        section "archives"]
    (-> page-data
        (assoc-in [:page-data :active] section)
        (assoc :content (assoc generic-page :title "Archives")
               :posts-data (->> (db/get-all-years querier)
                                (map #(reversed-two-layer-grouping
                                       querier db/get-year-posts % :month))
                                (into (sorted-map-by desc-str)))))))

(defn categories
  "Get all categories, get the post-keys for each category, then query for the
  required data for each post-key (done by the `listing-data` function)."
  [system]
  (log/info "Assembling data for categories listing page ...")
  (let [page-data (common system)
        querier (db-component/db-querier system)
        section "categories"]
    (-> page-data
        (assoc-in [:page-data :active] section)
        (assoc :content (assoc generic-page :title "Categories")
               :posts-data (->> (db/get-all-categories querier)
                                (map #(simple-grouping
                                       querier db/get-category-posts %))
                                (into (sorted-map)))))))

(defn tags
  "Get all tags, get the post-keys for each tag, then query for the
  required data for each post-key (done by the `listing-data` function)."
  [system]
  (log/info "Assembling data for tags listing page ...")
  (let [page-data (common system)
        querier (db-component/db-querier system)
        section "tags"]
    (-> page-data
        (assoc-in [:page-data :active] section)
        (assoc :content (assoc generic-page :title "Tags")
               :posts-data (->> (db/get-all-tags querier)
                                (map #(simple-grouping
                                       querier db/get-tag-posts %))
                                (into (sorted-map)))))))

(defn authors
  "Get all authors, get the post-keys for each author, then query for the
  required data for each post-key (done by the `listing-data` function)."
  [system]
  (log/info "Assembling data for authors listing page ...")
  (let [page-data (common system)
        querier (db-component/db-querier system)
        section "authors"]
    (-> page-data
        (assoc-in [:page-data :active] section)
        (assoc :content (assoc generic-page :title "Authors")
               :posts-data (->> (db/get-all-authors querier)
                                (map #(simple-grouping
                                       querier db/get-author-posts %))
                                (into (sorted-map)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Design Pages Data   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn design
  [system]
  (-> system
      common
      (assoc-in [:page-data :active] "design")
      (assoc :content (assoc generic-page :title "Design"))))
