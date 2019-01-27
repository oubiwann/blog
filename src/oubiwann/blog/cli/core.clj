(ns oubiwann.blog.cli.core
  (:require [dragon.event.system.core :as event]
            [dragon.event.tag :as tag]
            [oubiwann.blog.cli.new :as new]
            [oubiwann.blog.cli.show :as show]
            [oubiwann.blog.cli.share :as share]
            [oubiwann.blog.core :as core]
            [taoensso.timbre :as log]
            [trifl.docs :as docs]))

(defn run
  "
  Usage:
  ```
    ob COMMAND [help | arg...]
    ob [-h | --help | -v | --version]
  ```

  Commands:
  ```
    new      Create stubbed files for a new blog post
    show     Display various ob data in the terminal
    gen      Generate updated static content for blog
    share    Post blog updates (saved in files) to various services
    run      Run the ob Blog locally as a Ring app
    help     Display this usage message
    version  Display the current NOWA version
  ```

  More information:

    Each command takes an optional 'help' subcommand that will provide
    usage information about the particular command in question, e.g.:

  ```
    $ ob new help
  ```"
  [system [cmd & args]]
  (log/debug "CLI got cmd:" cmd)
  (log/debug "CLI got args:" args)
  (event/publish system tag/run-cli {:cmd cmd :args args})
  (case cmd
    :new (new/run system args)
    :show (show/run system args)
    :gen (core/generate system)
    :share (share/run system args)
    :run (core/generate system)
    :help (docs/print-docstring #'run)
    :version (print (core/version))
    ;; Aliases
    :--help (docs/print-docstring #'run)
    :--version (print (core/version))
    :-h (docs/print-docstring #'run)
    :-v (print (core/version)))
  (event/publish system tag/shutdown-cli))
