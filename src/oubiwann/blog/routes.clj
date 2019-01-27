(ns oubiwann.blog.routes
  "The routes for the blog need to take into consideration the following:

   * Actual posts will be generated behind the scenes when processing on-disk
     content (e.g., when calling `process-all-by-year-and-month`).
   * The routes are only used during development, when serving content
     dynamically.
   * Since the posts have already been generated and saved to disc, their
     routes should be generated dynamically as URI path / slurp call pairs."
  (:require [clojusc.twig :as twig]
            [dragon.blog.core :as blog]
            [dragon.components.config :as config]
            [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [oubiwann.blog.reader :as reader]
            [oubiwann.blog.sitemapper :as sitemapper]
            [oubiwann.blog.web.content.page :as page]
            [taoensso.timbre :as log]))

(defn static-routes
  ([system posts]
    (static-routes system posts {}))
  ([system posts routes]
    (merge
      routes
      {"/blog/about.html" (page/about system posts)
       "/blog/contact.html" (page/contact system posts)
       "/blog/powered-by.html" (page/powered-by system posts)
       "/blog/license.html" (page/license system posts)})))

(defn design-routes
  [system posts routes]
  (merge
    routes
    {"/blog/design/index.html" (page/design system posts)
     "/blog/design/bootstrap-theme.html" (page/bootstrap-theme system posts)
     "/blog/design/example-blog-home.html" (page/front-page-example system posts)
     "/blog/design/example-blog-post.html" (page/blog-example system posts)}))

(defn post-routes
  [system posts routes]
  (merge
    routes
    (blog/get-indexed-archive-routes
      (map vector (iterate inc 0) posts)
      :gen-func (partial page/post system posts)
      :uri-base (config/posts-path system))))

(defn index-routes
  [system posts routes]
  (merge
    routes
    {"/blog/index.html" (page/front-page system posts)
     "/blog/archives/index.html" (page/archives system posts)
     "/blog/categories/index.html" (page/categories system posts)
     "/blog/tags/index.html" (page/tags system posts)
     "/blog/authors/index.html" (page/authors system posts)}))

(defn reader-routes
  [system posts routes]
  (let [route "/blog/atom.xml"]
    (merge
      routes
      {route (reader/atom-feed
               system
               route
               (take (config/feed-count system) posts))})))

(defn sitemaps-routes
  [system routes]
  (let [route "/blog/sitemap.xml"]
    (merge
      routes
      {route (sitemapper/gen routes)})))

(defn routes
  [system posts]
  (log/trace "Got data:" (twig/pprint (blog/data-for-logs system posts)))
  (event/publish system tag/generate-routes-pre)
  (->> (static-routes system posts)
       (design-routes system posts)
       (post-routes system posts)
       (index-routes system posts)
       (reader-routes system posts)
       (sitemaps-routes system)
       (event/publish->> system tag/generate-routes-post)
       vec))

;;; Generator routes

(defn gen-route
  [func msg & args]
  (log/info msg)
  (apply func args))

(def gen-static-routes
  (partial
    gen-route
    static-routes
    "\tGenerating pages for static pages ..."))

(def gen-design-routes
  (partial
    gen-route
    design-routes
    "\tGenerating pages for design pages ..."))

(def gen-post-routes
  (partial
    gen-route
    post-routes
    "\tGenerating pages for blog posts ..."))

(def gen-index-routes
  (partial
    gen-route
    index-routes
    "\tGenerating pages for front page, archives, categories, etc. ..."))

(def gen-reader-routes
  (partial
    gen-route
    reader-routes
    "\tGenerating XML for feeds ..."))

(def gen-sitemaps-routes
  (partial
    gen-route
    sitemaps-routes
    "\tGenerating XML for sitemap ..."))

(defn gen-routes
  [system posts]
  (log/info "Generating routes ...")
  (log/trace "Got data:" (twig/pprint (blog/data-for-logs system posts)))
  (event/publish system tag/generate-routes-pre)
  (->> (gen-static-routes system posts)
       (gen-design-routes system posts)
       (gen-post-routes system posts)
       (gen-index-routes system posts)
       (gen-reader-routes system posts)
       (gen-sitemaps-routes system)
       (event/publish->> system tag/generate-routes-post)
       vec))
