(defn get-banner
  []
  (str
    (slurp "resources/text/banner.txt")
    #_(slurp "resources/text/loading.txt")))

(defn get-prompt
  [ns]
  (str "\u001B[35m[\u001B[34m"
       ns
       "\u001B[35m]\u001B[33m =>\u001B[m "))

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
    [com.cognitect/transit-clj]
    [com.fasterxml.jackson.core/jackson-core]
    [org.clojure/clojure]
    [org.clojure/clojurescript]
    [org.clojure/core.async]
    [org.clojure/tools.analyzer.jvm]
    [org.slf4j/slf4j-nop]]
  :dependencies [
    [ch.qos.logback/logback-classic "1.2.3"]
    [clojusc/rfc5322 "0.4.0"]
    [clojusc/system-manager "0.3.0"]
    [clojusc/trifl "0.4.2"]
    [clojusc/twig "0.4.1"]
    [com.cognitect/transit-clj "0.8.313"]
    ;; XXX Remove the following once the next dragon snapshot is pushed to Clojars
    [com.datomic/clj-client "0.8.606"]
    [com.fasterxml.jackson.core/jackson-core "2.9.8"]
    [com.taoensso/carmine "2.19.1"]
    ;; XXX END
    [com.stuartsierra/component "0.4.0"]
    [dragon "0.6.0-SNAPSHOT"]
    [markdown-clj "1.0.7"]
    [org.clojure/clojure "1.10.0"]
    [org.clojure/core.async "0.4.490"]
    [org.clojure/data.generators "0.1.2"]
    [org.clojure/data.xml "0.0.8"]
    [org.clojure/math.combinatorics "0.1.4"]
    [org.clojure/tools.analyzer.jvm "0.7.2"]
    [ring/ring-core "1.7.1"]
    [selmer "1.12.5"]
    [stasis "2.4.0"]]
  :source-paths ["src"]
  :profiles {
    :ubercompile {
      :aot :all}
    :custom-repl {
      :repl-options {
        :init-ns oubiwann.blog.repl
        :prompt ~get-prompt
        :init ~(println (get-banner))}}
    :dev {
      :source-paths ["dev-resources/src"]
      :main oubiwann.blog.main
      :plugins [
        [lein-shell "0.5.0"]
        [lein-simpleton "1.3.0"]]
      :dependencies [
        [expound "0.7.2"]
        [http-kit "2.3.0"]
        [inspectable "0.2.2"]
        [org.clojure/tools.namespace "0.2.11"]]
      ;:pedantic? :warn
      }
        :lint {
      :plugins [
        [jonase/eastwood "0.3.5"]
        [lein-kibit "0.1.6"]]}
    :test {
      :dependencies [
        [clojusc/ltest "0.4.0-SNAPSHOT"]]
      :plugins [
        [lein-ancient "0.6.15"]
        [lein-ltest "0.3.0"]]
      :test-selectors {
        :select :select}}
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
    "ubercompile" ["with-profile" "+ubercompile" "compile"]
    "check-vers" ["with-profile" "+test" "ancient" "check" ":all"]
    "check-jars" ["with-profile" "+test" "do"
      ["deps" ":tree"]
      ["deps" ":plugin-tree"]]
    "check-deps" ^{:doc "Check if any deps have out-of-date versions"}
    ["do"
      ["check-jars"]
      ["check-vers"]]
    "kibit" ["with-profile" "+lint" "kibit"]
    "eastwood" ["with-profile" "+lint" "eastwood" "{:namespaces [:source-paths]}"]
    "lint" ^{:doc "Perform lint checking"}
      ["do"
        ["kibit"]
        ;["eastwood"]
        ]
    "ltest" ["with-profile" "+test" "ltest"]
    "init-content"
      ^{:doc "Add blog content branch as a submodule"}
      ["shell" "git" "submodule" "update" "--init" "--recursive"]
    "repl"
      ^{:doc "A custom REPL that overrides the default one"}
      ["with-profile" "+test,+custom-repl,+cli" "repl"]
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
