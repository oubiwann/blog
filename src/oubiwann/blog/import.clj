(ns oubiwann.blog.import
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojusc.blogger.xml.parser.export :as blogger]
    [taoensso.timbre :as log]
    [trifl.fs :as fs]))

(defn dragon-headers
  [post-data]
  ;; XXX Create string for post headers
  (format (str "Subject: %s\n"
               "Subtitle: \n"
               "Header-Image: \n"
               "Excerpt: \n"
               "From: oubiwann\n"
               "Twitter: oubiwann\n"
               "Category: \n"
               "Keywords: %s\n"
               "Source: %s\n"
               "Comments:\n"
               "Content-Type: md\n"
               "Public?: %s\n\n")
          (:title post-data)
          (string/join "," (:tags post-data))
          (:url post-data)
          (if (:draft post-data) false true)))

(defn date-dirs
  [post-data]
  (let [published (:published post-data)
        s (.getSeconds published)]
    (format "%d-%02d/%02d-%02d%02d%02d"
            (+ 1900 (.getYear published))
            (inc (.getMonth published))
            (.getDate published)
            (.getHours published)
            (.getMinutes published)
            (if (zero? s) 8))))

(defn write-post
  [post-data]
  (let [filedata (str (dragon-headers post-data)
                      (:content post-data))
        filename (format "posts/%s/content.rfc5322" (date-dirs post-data))]
    (log/tracef "Writing file data:\n%s\n ..." filedata)
    (log/debug "Wrote post file:" filename)
    (io/make-parents filename)
    (spit filename filedata)))

(defn import-blogger
  []
  (blogger/disable-xml-ns!)
  (->> "import/blog-01-22-2019.xml"
       blogger/xml-resource->zip
       blogger/extract-posts
       (take 1)
       (mapv write-post)
       :ok))
