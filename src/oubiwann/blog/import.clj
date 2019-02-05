(ns oubiwann.blog.import
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojusc.blogger.xml.parser.export :as blogger]
    [taoensso.timbre :as log]
    [trifl.fs :as fs])
  (:import
    (java.time ZoneId)))

(def ^{:dynamic true} *last-datestamp* #inst"1900-01-01T00:00:00.000-00:00")

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
  (let [raw-date (:published post-data)
        published (-> raw-date
                      (.toInstant)
                      (.atZone (ZoneId/systemDefault)))
        s (.getSecond published)]
    (when (= *last-datestamp* raw-date)
      (log/warn "Duplicate post timestamp detected ...")
      (log/error "Overwriting old post ..."))
    (alter-var-root #'*last-datestamp* (constantly published))
    (format "%d-%02d/%02d-%02d%02d%02d"
            (.getYear published)
            (.getMonthValue published)
            (.getDayOfMonth published)
            (.getHour published)
            (.getMinute published)
            (if (zero? s) 8 s))))

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
       (mapv write-post)
       :ok))
