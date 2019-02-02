# oubiwann | blog

*A 21st century .plan for Duncan McGreggor*

Visit: [https://oubiwann.github.io/blog/](https://oubiwann.github.io/blog/)


## Prerequisites

* GNU `make`
* The `sass` CSS compiler (which requires having `npm` installed)

Set the `PATH` to include the project's executable and setup auto-completion:

```bash
$ export PATH=$PATH:`pwd`/bin
$ source dev-resources/shell/ob-bash-autocompletion
```


## Creating Post Stubs

```bash
$ ob new post md
```

or, for example,

```bash
$ ob new post rfc5322
```

For more options see `ob new post help`.


## Generating Static Files

```bash
$ ob gen
```

If you'd like to run a dev web server with the generated content served at the
doc root, you can use this `make` target:

```
$ make blog-dev-gen
```


## Checking Metadata and Content

TBD


## Developing Content

Start up the REPL:

```
$ lein repl
```

Regenerate the content and start the local dev server:

```clj
(core/generate+web)
```

Edit files, reload the Clojure namespaces, and regenerate the content:

```clj
(reload)
(core/generate)
```
