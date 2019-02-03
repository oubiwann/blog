(ns oubiwann.blog.util
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojusc.blogger.xml.parser.export :as blogger]
    [dragon.blog.content.core :as content]
    [taoensso.timbre :as log]
    [trifl.fs :as fs])
  (:import
    (java.io ByteArrayInputStream)))

(defn zip
  [& colls]
  (partition (count colls)
             (apply interleave colls)))

(defn get-post-data
  [post-file]
  (log/debugf "Getting content for %s ..." post-file)
  (-> post-file
      (io/resource)
      (.getFile)
      (io/file)
      (content/parse)))

(defn get-message-content-file
  [post-file msg-filename]
  (let [parent-dir (fs/parent (io/file post-file))]
    (->> msg-filename
         (format "%s/%s" parent-dir)
         (io/resource)
         (.getFile)
         (io/file))))

(defn read-home-file
  [^String file-name]
  (-> file-name
      fs/expand-home
      io/file))

(defn home-file->str
  [^String file-name]
  (-> file-name
      read-home-file
      slurp
      string/trim-newline))

(defn home-file->stream
  [^String file-name]
  (-> file-name
      read-home-file
      io/input-stream))

(defn str->stream
  [data]
  (->> data
       (map int)
       (byte-array)
       (io/input-stream)))

(defn str->reader
  [data]
  (->> data
       (str->stream)
       (io/reader)))

(defn write-pem-file
  [data]
  (fs/write-tmp-file! "key.pem" data))

(defn delete-pem-file
  [file-obj]
  (io/delete-file file-obj)
  (-> file-obj
      (fs/parent)
      (io/file)
      (io/delete-file)))

(defn blogger-headers
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

(defn make-post-dirs
  [post-data]
  (let [published (:published post-data)
        s (.getSeconds published)
        dirs (format "%d-%02d/%02d-%02d%02d%02d"
                     (+ 1900 (.getYear published))
                     (inc (.getMonth published))
                     (.getDate published)
                     (.getHours published)
                     (.getMinutes published)
                     (if (zero? s) 8))]
    (log/warn "Creating post directory:" dirs)
    ;; XXX create dir
    )
  post-data)

(defn write-post-file
  [post-data]
  (let [file-data (str (blogger-headers post-data)
                       (:content post-data))]
    (log/warn file-data)
    ;; XXX write data to file
    )
  post-data)

(defn convert-blogger-post
  [post-data]
  (-> post-data
      make-post-dirs
      write-post-file)
  :ok)

(defn blogger-import
  []
  (blogger/disable-xml-ns!)
  (->> "import/blog-01-22-2019.xml"
       blogger/xml-resource->zip
       blogger/extract-posts
       (take 10)
       (mapv convert-blogger-post)))
