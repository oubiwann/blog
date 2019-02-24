(ns oubiwann.blog.routes
  "The routes for the blog need to take into consideration the following:

   * Actual posts will be generated behind the scenes when processing on-disk
     content (e.g., when calling `process-all-by-year-and-month`).
   * The routes are only used during development, when serving content
     dynamically.
   * Since the posts have already been generated and saved to disc, their
     routes should be generated dynamically as URI path / slurp call pairs."
  (:require
    [clojure.string :as string]
    [clojusc.twig :as twig]
    [dragon.blog.core :as blog]
    [dragon.components.config :as config]
    [dragon.components.db :as db-component]
    [dragon.data.sources.core :as db]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [oubiwann.blog.reader :as reader]
    [oubiwann.blog.sitemapper :as sitemapper]
    [oubiwann.blog.web.content.page :as page]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post-route?
  [system route]
  (string/starts-with? route
                       (config/posts-path system)))

(defn split-route
  ([system route]
    (split-route system (db-component/db-querier system) route))
  ([system querier route]
    (let [prefix (str (config/posts-path system) "/")]
    (->> prefix
         re-pattern
         (string/split route)
         (concat [prefix])))))

(defn route->file
  ([system route]
    (route->file system (db-component/db-querier system) route))
  ([system querier route]
    (let [[prefix _ uri-path] (split-route system querier route)
          src-file (db/uri-path->file querier uri-path)]
      src-file)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Route/Content Mappers   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn static-routes
  ([system]
    (static-routes system {}))
  ([system routes]
    (log/info "Assembling routes for 'About' section (markdown) ...")
    (merge
      routes
      {"/blog/about/index.html" (page/about system)
       "/blog/about/contact/index.html" (page/contact system)
       "/blog/about/powered-by/index.html" (page/powered-by system)
       "/blog/about/license/index.html" (page/license system)})))

(defn design-routes
  [system routes]
  (log/info "Assembling routes for 'Design' section (HTML + markdown) ...")
  (merge
    routes
    {"/blog/design/index.html" (page/design system)
     "/blog/design/bootswatch-theme.html" (page/bootstrap-theme system)
     "/blog/design/home.html" (page/front-page-example system)
     "/blog/design/post.html" (page/blog-post-example system)
     "/blog/design/code-highlight-samples.html" (page/code-highlight system)}))

(defn post-routes
  [system routes]
  (log/info "Assembling routes for all blog posts ...")
  (let [querier (db-component/db-querier system)]
    (merge
      routes
      (->> (db/get-keys querier)
           (map #(page/post system %))
           (interleave (->> querier
                            (db/get-all-uri-paths)
                            (map #(format "%s/%s"
                                          (config/posts-path system) %))))
           (partition 2)
           (map vec)
           (into {})))))

(defn listing-routes
  [system routes]
  (log/info "Assembling routes for main and listing pages ...")
  (merge
    routes
    {"/blog/index.html" (page/front-page system)
     "/blog/archives/index.html" (page/archives system)
     "/blog/categories/index.html" (page/categories system)
     "/blog/tags/index.html" (page/tags system)
     "/blog/authors/index.html" (page/authors system)}))

(defn reader-routes
  [system routes]
  (log/info "Assembling routes for Atom/RSS ...")
  (let [route "/blog/atom.xml"]
    (merge
      routes
      {route (reader/atom-feed
               system
               route)})))

(defn sitemaps-routes
  [system routes]
  (log/info "Assembling routes for sitemaps ...")
  (let [route "/blog/sitemap.xml"]
    (merge
      routes
      {route (sitemapper/generate routes)})))

(defn static-route
  [system route]
  (case route
    "/blog/index.html" {route (page/front-page system)}
    "/blog/archives/index.html" {route (page/archives system)}
    "/blog/categories/index.html" {route (page/categories system)}
    "/blog/tags/index.html" {route (page/tags system)}
    "/blog/authors/index.html" {route (page/authors system)}
    "/blog/about/index.html" {route (page/about system)}
    "/blog/about/contact/index.html" {route (page/contact system)}
    "/blog/about/powered-by/index.html" {route (page/powered-by system)}
    "/blog/about/license/index.html" {route (page/license system)}
    "/blog/design/index.html" {route (page/design system)}
    "/blog/design/bootswatch-theme.html" {route (page/bootstrap-theme system)}
    "/blog/design/home.html" {route (page/front-page-example system)}
    "/blog/design/post.html" {route (page/blog-post-example system)}
    "/blog/design/code-highlight-samples.html" {route (page/code-highlight
                                                       system)}))

(defn post-route
  [system route]
  {route (page/post system (route->file system route))})

(defn one
  [system route]
  (if (post-route? system route)
    (post-route system route)
    (static-route system route)))

(defn all
  [system]
  (event/publish system tag/generate-routes-pre)
  (->> system
       (static-routes)
       (design-routes system)
       (post-routes system)
       (listing-routes system)
       (reader-routes system)
       (sitemaps-routes system)
       (event/publish->> system tag/generate-routes-post)
       vec))
