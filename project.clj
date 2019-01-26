(defn get-banner
  []
  ; (str
  ;   (slurp "resources/text/repl-banner.txt")
  ;   (slurp "resources/text/repl-loading.txt")))
  "")

(defn get-prompt
  [ns]
  (str "\u001B[35m[\u001B[34m"
       ns
       "\u001B[35m]\u001B[33m λ\u001B[m=> "))

(defproject oubiwann/blog "0.1.0-SNAPSHOT"
  :description "A 21st century .plan for Duncan McGreggor"
  :url "https://oubiwann.github.io/blog/"
  :scm {
    :name "git"
    :url "https://github.com/oubiwann/blog"}
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :exclusions [
    [org.clojure/clojure]
    [org.clojure/clojurescript]]
  :dependencies [
    [clojusc/rfc5322 "0.4.0"]
    [clojusc/trifl "0.4.2"]
    [clojusc/twig "0.4.0"]
    ;; XXX Remove the following once the next dragon snapshot is pushed to Clojars
    [com.datomic/clj-client "0.8.606"]
    [com.taoensso/carmine "2.19.1"]
    ;; XXX END
    [com.stuartsierra/component "0.4.0"]
    [dragon "0.5.0"]
    [markdown-clj "1.0.7"]
    [org.clojure/clojure "1.10.0"]
    [org.clojure/data.generators "0.1.2"]
    [org.clojure/data.xml "0.0.8"]
    [org.clojure/math.combinatorics "0.1.4"]
    [ring/ring-core "1.7.1"]
    [selmer "1.12.5"]
    [stasis "2.4.0"]]
  :source-paths ["src/clj"]
  :dragon {
    :domain "oubiwann.github.io/blog"
    :name "oubiwann :: blog"
    :description "The 21st century .plan for Duncan McGreggor"
    :port 5096
    :output-dir "."
    :base-path "/blog"
    :posts-path "/blog/archives"
    :posts-path-src "./posts"
    :feed-count 20
    :cli {
      :log-level :info
      :log-nss [oubiwann.blog]}}
  :profiles {
    :uberjar {:aot :all}
    :custom-repl {
      :repl-options {
        :init-ns oubiwann.blog.dev
        :prompt ~get-prompt
        ;:init ~(println (get-banner))
        }}
    :dev {
      :source-paths ["dev-resources/src"]
      :main oubiwann.blog.main
      :plugins [
        [lein-shell "0.5.0"]
        [lein-simpleton "1.3.0"]]
      :dependencies [
        [http-kit "2.3.0"]
        [org.clojure/tools.namespace "0.2.11"]]
      ;:pedantic? :warn
      }
    :test {
      :plugins [
        [lein-ancient "0.6.15"]
        [jonase/eastwood "0.3.4" :exclusions [org.clojure/clojure]]
        [lein-kibit "0.1.6" :exclusions [org.clojure/clojure]]]}
    :cli {
      :resource-paths ["posts"]
      :exclusions [
        clj-http
        clojusc/cljs-tools
        common-codec
        commons-logging
        joda-time
        org.apache.maven.wagon/wagon-http
        org.clojure/clojurescript
        org.clojure/clojure]
      :dependencies [
        [clj-http "3.9.1"]
        [clojusc/cljs-tools "0.2.1"]
        [com.draines/postal "2.0.3"]
        [com.google.api-client/google-api-client "1.28.0"]
        [com.google.apis/google-api-services-plusDomains "v1-rev20180805-1.28.0"]
        [commons-codec "1.11"]
        [commons-logging "1.2"]
        [joda-time "2.10.1"]
        [org.apache.maven.wagon/wagon-http "3.3.1"]
        [org.clojure/data.json "0.2.6"]
        [twitter-api "1.8.0"]]}}
  :aliases {
    "init-content"
      ^{:doc "Add blog content branch as a submodule"}
      ["shell" "git" "submodule" "update" "--init" "--recursive"]
    "repl"
      ^{:doc "A custom REPL that overrides the default one"}
      ["with-profile" "+test,+custom-repl,+cli" "repl"]
    "check-deps"
      ^{:doc "Check if any deps have out-of-date versions"}
      ["with-profile" "+test" "ancient" "check" ":all"]
    "lint"
      ^{:doc "Perform lint checking"}
      ["with-profile" "+test" "kibit"]
    "ob"
      ^{:doc "The blog CLI; type `lein ob help` or `ob help` for commands"}
      ["with-profile" "+cli"
       "run" "-m" "oubiwann.blog.main" "cli"]
    "gen"
      ^{:doc "Generate static content for the blog"}
      ["run" "-m" "oubiwann.blog.core/generate"]
    "web"
      ^{:doc "Run a simple web server with docroot of generated content"}
      ["simpleton" "5099" "file" ":from" "."]
    "dev"
      ^{:doc "Generate blog content and run local web service"}
      ["run" "-m" "oubiwann.blog.core/log+generate+web"]
    "build-sass"
      ^{:doc "Generate blog theme from SCSS files"}
      ["shell" "sass" "blog/scss/theme.scss" "blog/css/theme.css"]
    "build"
      ^{:doc "Perform build tasks for CI/CD & releases\n\nAdditional aliases:"}
      ["with-profile" "+test,+cli" "do"
        ;["check-deps"]
        ;["lint"]
        ["test"]
        ["compile"]
        ["uberjar"]]})
