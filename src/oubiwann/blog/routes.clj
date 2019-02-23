(ns oubiwann.blog.routes
  "The routes for the blog need to take into consideration the following:

   * Actual posts will be generated behind the scenes when processing on-disk
     content (e.g., when calling `process-all-by-year-and-month`).
   * The routes are only used during development, when serving content
     dynamically.
   * Since the posts have already been generated and saved to disc, their
     routes should be generated dynamically as URI path / slurp call pairs."
  (:require
    [clojusc.twig :as twig]
    [dragon.blog.core :as blog]
    [dragon.components.config :as config]
    [dragon.event.system.core :as event]
    [dragon.event.tag :as tag]
    [oubiwann.blog.reader :as reader]
    [oubiwann.blog.sitemapper :as sitemapper]
    [oubiwann.blog.web.content.page :as page]
    [taoensso.timbre :as log]))

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
     "/blog/design/bootstrap-theme.html" (page/bootstrap-theme system)
     "/blog/design/front-page-example.html" (page/front-page-example system)
     "/blog/design/blog-post-example.html" (page/blog-post-example system)
     "/blog/design/code-highlight-samples.html" (page/code-highlight system)}))

(defn post-routes
  [system routes]
  (log/info "Assembling routes for all blog posts ...")
  (merge
    routes
    {}))
    ; (blog/get-indexed-archive-routes
    ;   (map vector (iterate inc 0) posts)
    ;   :gen-func (partial page/post system posts)
    ;   :uri-base (config/posts-path system))))

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
  [system posts routes]
  (log/info "Assembling routes for Atom/RSS ...")
  (let [route "/blog/atom.xml"]
    (merge
      routes
      {route (reader/atom-feed
               system
               route
               (take (config/feed-count system) posts))})))

(defn sitemaps-routes
  [system routes]
  (log/info "Assembling routes for sitemaps ...")
  (let [route "/blog/sitemap.xml"]
    (merge
      routes
      {route (sitemapper/generate routes)})))

(defn all
  [system]
  (event/publish system tag/generate-routes-pre)
  (->> system
       (static-routes)
       (design-routes system)
       ; (post-routes system)
       (listing-routes system)
       ; (reader-routes system)
       (sitemaps-routes system)
       (event/publish->> system tag/generate-routes-post)
       vec))
