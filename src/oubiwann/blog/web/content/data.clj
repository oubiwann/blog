(ns oubiwann.blog.web.content.data
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [dragon.blog.core :as blog]
    [dragon.blog.tags :as tags]
    [dragon.components.config :as config]
    [dragon.components.db :as db-component]
    [dragon.data.sources.core :as db]
    [markdown.core :as markdown]
    [oubiwann.blog.util :as util]
    [taoensso.timbre :as log])
  (:import
    (java.util Random)))

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

;; XXX remove this function and all references to it; no longer needed
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

(defn random-text-idx
  [text max-count]
  (inc (.nextInt (new Random (.hashCode text)) max-count)))

(defn common-post-data
  [querier post-key]
  {:url-path (format "%s/%s"
                     (config/posts-path (:component querier))
                     (db/get-post-uri-path querier post-key))})

(defn archive-post-data
  "Headline pages need the following:

  * post category (get-post-category)
  * post dates (get-post-dates)
  * post metadata (get-post-metadata)
  * post tags (get-post-tags)
  * post URL (get-post-uri-path)"
  [querier post-key]
  (merge
    (common-post-data querier post-key)
    {:category (db/get-post-category querier post-key)
     :dates (db/get-post-dates querier post-key)
     :metadata (db/get-post-metadata querier post-key)
     :tags (db/get-post-tags querier post-key)}))

(defn headline-post-data
  "Headline pages need the following:

  * post category (get-post-category)
  * post dates (get-post-dates)
  * post excerpts (get-post-excerpts)
  * post metadata (get-post-metadata)
  * post tags (get-post-tags)
  * post URL (get-post-uri-path)"
  [querier post-key]
  (merge
    (archive-post-data querier post-key)
    {:excerpts (db/get-post-excerpts querier post-key)}))

(defn listing-post-data
  "Listing pages need the following:

  * year, month, day (get-post-dates)
  * post URL (get-url-path)
  * post title (get-post-metadata)
  * post subtitle (get-post-metadata)"
  [querier post-key]
  (let [dates (db/get-post-dates querier post-key)
        metadata (db/get-post-metadata querier post-key)]
    (merge
      (common-post-data querier post-key)
      {:month-name (:month dates)
       :year (get-in dates [:date :year])
       :month (get-in dates [:date :month])
       :day (get-in dates [:date :day])
       :subtitle (:subtitle metadata)
       :title (:title metadata)})))

(defn desc-str
  [a b]
  (compare b a))

(defn simple-grouping
  [querier data-fn grouping-key]
  [grouping-key (->> grouping-key
                     (data-fn querier)
                     (map #(listing-post-data querier %)))])

(defn two-layer-grouping
  [querier data-fn grouping-key second-level-key]
  [grouping-key (->> grouping-key
                     (data-fn querier)
                     (map #(listing-post-data querier %))
                     (group-by second-level-key)
                     sort
                     (into (sorted-map)))])

(defn reversed-two-layer-grouping
  [querier data-fn grouping-key second-level-key]
  [grouping-key (->> grouping-key
                     (data-fn querier)
                     (map #(listing-post-data querier %))
                     (group-by second-level-key)
                     sort
                     (into (sorted-map-by desc-str)))])

(defn supporting-headlines
  [system posts-data]
  (subvec posts-data
          1
          (inc (config/headlines-supporting-count system))))

(defn middle-headlines
  [system posts-data]
  (let [supporting (inc (config/headlines-supporting-count system))]
    (log/trace "Supporting headlines:" supporting)
    (subvec posts-data
            supporting
            (+ supporting
               (config/headlines-middle-count system)))))

(defn trailing-headlines
  [system posts-data]
  (let [supporting (inc (config/headlines-supporting-count system))
        middle (+ supporting
                  (config/headlines-middle-count system))]
    (log/trace "Supporting headlines:" supporting)
    (log/trace "Middle headlines:" middle)
    (subvec posts-data
            middle
            (+ middle
               (config/headlines-trailing-count system)))))

(defn update-with-img-index
  [post-data image-count]
  (assoc post-data
         :img-idx
         (random-text-idx
          (get-in post-data [:metadata :title]) image-count)))

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
  [system post-key]
  (let [page-data (common system)
        querier (db-component/db-querier system)
        default-images {
          :post (config/default-images-post-tmpl system)
          :small (config/default-images-small-tmpl system)
          :thumb (config/default-images-thumb-tmpl system)}
        images-count (config/default-images-count system)
        post-data (update-with-img-index
                   (db/get-all-data querier post-key)
                   images-count)
        header-image (format "%s%s.jpg"
                             (:post default-images)
                             (:img-idx post-data))]
    (-> page-data
        (assoc-in [:page-data :active] "archives")
        (assoc :post-data (assoc post-data
                                 :header-image header-image)
               :default-images default-images))))

(defn front-page
  [system]
  (log/info "Assembling data for front page ...")
  (let [page-data (common system)
        querier (db-component/db-querier system)
        section "index"
        default-img-count (config/default-images-count system)
        posts-data (->> (config/headlines-count system)
                        (db/get-last-n-keys querier)
                        (map #(headline-post-data querier %))
                        (map #(update-with-img-index % default-img-count))
                        vec)]
    (-> page-data
        (assoc-in [:page-data :active] section)
        (assoc :default-images {
                 :headliner (config/default-images-headliner-tmpl system)
                 :small (config/default-images-small-tmpl system)
                 :thumb (config/default-images-thumb-tmpl system)}
               :headliner (first posts-data)
               :supporting (supporting-headlines system posts-data)
               :middle (middle-headlines system posts-data)
               :trailing (partition
                          (config/headlines-trailing-per-row system)
                          (trailing-headlines system posts-data))))))

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
