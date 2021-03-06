(ns oubiwann.blog.web.content.page
  (:require
    [dragon.selmer.core :as template]
    [dragon.util :as util]
    [oubiwann.blog.web.content.data :as data]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Static Pages   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn about
  [system]
  (template/render
    "templates/pages/generic.html"
    (data/about system)))

(defn contact
  [system]
  (template/render
    "templates/pages/generic.html"
    (data/contact system)))

(defn powered-by
  [system]
  (template/render
    "templates/pages/generic.html"
    (data/powered-by system)))

(defn license
  [system]
  (template/render
    "templates/pages/generic.html"
    (data/license system)))

(defn code-highlight
  [system]
  (template/render
    "templates/pages/generic.html"
    (data/code-highlight system)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dynamic Pages   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post
  [system post-key]
  (template/render
    "templates/pages/post.html"
    (data/post system post-key)))

(defn front-page
  [system]
  (template/render
    "templates/pages/home.html"
    (data/front-page system)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Listings Pages   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn archives
  [system]
  (template/render
    "templates/listings/archives.html"
    (data/archives system)))

(defn categories
  [system]
  (template/render
    "templates/listings/categories.html"
    (data/categories system)))

(defn tags
  [system]
  (template/render
    "templates/listings/tags.html"
    (data/tags system)))

(defn authors
  [system]
  (template/render
    "templates/listings/authors.html"
    (data/authors system)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Design Pages   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn design
  [system]
  (template/render
    "templates/design/index.html"
    (data/design system)))

(defn bootstrap-theme
  [system]
  (template/render
    "templates/design/bootswatch-theme.html"
    (data/design system)))

(defn front-page-example
  [system]
  (template/render
    "templates/design/home.html"
    (data/design system)))

(defn blog-post-example
  [system]
  (template/render
    "templates/design/post.html"
    (data/design system)))
