(ns oubiwann.blog.web.content.page
  (:require [dragon.selmer.core :as template]
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
  [system posts post-data]
  (template/render
    "templates/pages/post.html"
    (data/post system posts post-data)))

(defn front-page
  [system posts]
  (let [above-fold 5
        below-fold 5
        headline-posts (->> posts
                            (filter util/headline?)
                            (take (+ above-fold below-fold)))]
    (template/render
      "templates/pages/home.html"
      (data/front-page
        system
        posts
        headline-posts
        :above-fold-count above-fold
        :below-fold-count below-fold
        :column-count 2))))

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
    "templates/design/bootstrap-theme.html"
    (data/design system)))

(defn front-page-example
  [system]
  (template/render
    "templates/design/front-page-example.html"
    (data/design system)))

(defn blog-post-example
  [system]
  (template/render
    "templates/design/blog-post-example.html"
    (data/design system)))
