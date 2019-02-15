<img src="/blog/img/clojure.png" alt="Clojure Logo" class="about-portrait img-responsive"></span>

In the early 2000s, this blog was powered by MovableType on a dedicated server
in a colo (Hurricane Electric, Fremont, CA). After a few years, I moved it to
[advogato.org](https://en.wikipedia.org/wiki/Advogato) where I'd been a member
since 2001. Then, for its longest run, it was on Google's
[Blogger platform](http://oubiwann.blogspot.com/). However, that didn't offer
the flexibility I needed, so I now use Github to publish the blog.

With the move to Github, I gained complete control over the entire chain from
content creation through publishing. This allowed me to use the same tooling
I'd been employing for several other projects over the past few years.

The code that generates this site is in a
[repository on Github](https://github.com/oubiwann/blog). It's written using
the following open source languages/libraries:

* [Clojure programming language](https://clojure.org/)
* The [Dragon](https://github.com/clojusc/dragon) static site generator,
  which in turn incorporates functionality from:
  * the [Stasis](https://github.com/magnars/stasis) library
  * [Selmer page templates](https://github.com/yogthos/Selmer) (Django-based)
  * and [more](https://github.com/clojusc/dragon/blob/master/project.clj#L10)
* The [{Sass}](https://sass-lang.com/) CSS compiler

The generated content is then published to a gh-pages branch in the Github
repo and served from there.

