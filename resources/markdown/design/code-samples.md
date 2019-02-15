
Demonstrating Highlightjs ...

Bash:

```
#!/bin/bash

###### CONFIG
ACCEPTED_HOSTS="/root/.hag_accepted.conf"
BE_VERBOSE=false

if [ "$UID" -ne 0 ]
then
 echo "Superuser rights required"
 exit 2
fi

genApacheConf(){
 echo -e "# Host ${HOME_DIR}$1/$2 :"
}
```
INI:

```
; boilerplate
[package]
name = "some_name"
authors = ["Author"]
description = "This is \
a description"

[[lib]]
name = ${NAME}
default = True
auto = no
counter = 1_000
```

Java:

```
/**
 * @author John Smith <john.smith@example.com>
*/
package l2f.gameserver.model;

public abstract class L2Char extends L2Object {
  public static final Short ERROR = 0x0001;

  public void moveTo(int x, int y, int z) {
    _ai = null;
    log("Should not be called");
    if (1 > 5) { // wtf!?
      return;
    }
  }
}
```

JavaScript:

```
function $initHighlight(block, cls) {
  try {
    if (cls.search(/\bno\-highlight\b/) != -1)
      return process(block, true, 0x0F) +
             ` class="${cls}"`;
  } catch (e) {
    /* handle exception */
  }
  for (var i = 0 / 2; i < classes.length; i++) {
    if (checkCondition(classes[i]) === undefined)
      console.log('undefined');
  }
}

export  $initHighlight;
```

JSON:

```
[
  {
    "title": "apples",
    "count": [12000, 20000],
    "description": {"text": "...", "sensitive": false}
  },
  {
    "title": "oranges",
    "count": [17500, null],
    "description": {"text": "...", "sensitive": false}
  }
]
```

Markdown:

```
# hello world

you can write text [with links](http://example.com) inline or [link references][1].

* one _thing_ has *em*phasis
* two __things__ are **bold**

[1]: http://example.com

---

hello world
===========

<this_is inline="xml"></this_is>

> markdown is so cool

    so are code segments

1. one thing (yeah!)
2. two thing `i can write code`, and `more` wipee!
```

Python:

```
@requires_authorization
def somefunc(param1='', param2=0):
    r'''A docstring'''
    if param1 > param2: # interesting
        print 'Gre\'ater'
    return (param2 - param1 + 1 + 0b10l) or None

class SomeClass:
    pass

>>> message = '''interpreter
... prompt'''
```

YAML:

```
---
# comment
string_1: "Bar"
string_2: 'bar'
string_3: bar
inline_keys_ignored: sompath/name/file.jpg
keywords_in_yaml:
  - true
  - false
  - TRUE
  - FALSE
  - 21
  - 21.0
  - !!str 123
"quoted_key": &foobar
  bar: foo
  foo:
  "foo": bar

reference: *foobar

multiline_1: |
  Multiline
  String
multiline_2: >
  Multiline
  String
multiline_3: "
  Multiline string
  "

ansible_variables: "foo {{variable}}"

array_nested:
- a
- b: 1
  c: 2
- b
- comment
```

Erlang:

```
-module(ssh_cli).

-behaviour(ssh_channel).

-include("ssh.hrl").
%% backwards compatibility
-export([listen/1, listen/2, listen/3, listen/4, stop/1]).

if L =/= [] ->      % If L is not empty
    sum(L) / count(L);
true ->
    error
end.

%% state
-record(state, {
    cm,
    channel
   }).

-spec foo(integer()) -> integer().
foo(X) -> 1 + X.

test(Foo)->Foo.

init([Shell, Exec]) ->
    {ok, #state{shell = Shell, exec = Exec}};
init([Shell]) ->
    false = not true,
    io:format("Hello, \"~p!~n", [atom_to_list('World')]),
    {ok, #state{shell = Shell}}.

concat([Single]) -> Single;
concat(RList) ->
    EpsilonFree = lists:filter(
        fun (Element) ->
            case Element of
                epsilon -> false;
                _ -> true
            end
        end,
        RList),
    case EpsilonFree of
        [Single] -> Single;
        Other -> {concat, Other}
    end.

union_dot_union({union, _}=U1, {union, _}=U2) ->
    union(lists:flatten(
        lists:map(
            fun (X1) ->
                lists:map(
                    fun (X2) ->
                        concat([X1, X2])
                    end,
                    union_to_list(U2)
                )
            end,
            union_to_list(U1)
        ))).
```

Erlang REPL:

```
1> Str = "abcd".
"abcd"
2> L = test:length(Str).
4
3> Descriptor = {L, list_to_atom(Str)}.
{4,abcd}
4> L.
4
5> b().
Descriptor = {4,abcd}
L = 4
Str = "abcd"
ok
6> f(L).
ok
7> b().
Descriptor = {4,abcd}
Str = "abcd"
ok
8> {L, _} = Descriptor.
{4,abcd}
9> L.
4
10> 2#101.
5
11> 1.85e+3.
1850
```

Clojure:

```
(def ^:dynamic chunk-size 17)

(defn next-chunk [rdr]
  (let [buf (char-array chunk-size)
        s (.read rdr buf)]
  (when (pos? s)
    (java.nio.CharBuffer/wrap buf 0 s))))

(defn chunk-seq [rdr]
  (when-let [chunk (next-chunk rdr)]
    (cons chunk (lazy-seq (chunk-seq rdr)))))
```

Clojure REPL:

```
user=> (defn f [x y]
  #_=>   (+ x y))
#'user/f
user=> (f 5 7)
12
user=> nil
nil
```

Hy:

```
#!/usr/bin/env hy

(import os.path)

(import hy.compiler)
(import hy.core)


;; absolute path for Hy core
(setv *core-path* (os.path.dirname hy.core.--file--))


(defn collect-macros [collected-names opened-file]
  (while True
    (try
     (let [data (read opened-file)]
       (if (and (in (first data)
                    '(defmacro defmacro/g! defn))
                (not (.startswith (second data) "_")))
         (.add collected-names (second data))))
     (except [e EOFError] (break)))))


(defmacro core-file [filename]
  `(open (os.path.join *core-path* ~filename)))


(defmacro contrib-file [filename]
  `(open (os.path.join *core-path* ".." "contrib" ~filename)))


(defn collect-core-names []
  (doto (set)
        (.update hy.core.language.*exports*)
        (.update hy.core.shadow.*exports*)
        (collect-macros (core-file "macros.hy"))
        (collect-macros (core-file "bootstrap.hy"))))
```

Lisp:

```
#!/usr/bin/env csi

(defun prompt-for-cd ()
   "Prompts
    for CD"
   (prompt-read "Title" 1.53 1 2/4 1.7 1.7e0 2.9E-4 +42 -7 #b001 #b001/100 #o777 #O777 #xabc55 #c(0 -5.6))
   (prompt-read "Artist" &rest)
   (or (parse-integer (prompt-read "Rating") :junk-allowed t) 0)
  (if x (format t "yes") (format t "no" nil) ;and here comment
  )
  ;; second line comment
  '(+ 1 2)
  (defvar *lines*)                ; list of all lines
  (position-if-not #'sys::whitespacep line :start beg))
  (quote (privet 1 2 3))
  '(hello world)
  (* 5 7)
  (1 2 34 5)
  (:use "aaaa")
  (let ((x 10) (y 20))
    (print (+ x y))
  )
```

Scheme:

```
;; Calculation of Hofstadter's male and female sequences as a list of pairs

(define (hofstadter-male-female n)
(letrec ((female (lambda (n)
           (if (= n 0)
           1
           (- n (male (female (- n 1)))))))
     (male (lambda (n)
         (if (= n 0)
             0
             (- n (female (male (- n 1))))))))
  (let loop ((i 0))
    (if (> i n)
    '()
    (cons (cons (female i)
            (male i))
      (loop (+ i 1)))))))

(hofstadter-male-female 8)

(define (find-first func lst)
(call-with-current-continuation
 (lambda (return-immediately)
   (for-each (lambda (x)
       (if (func x)
           (return-immediately x)))
         lst)
   #f)))
```

Maxima:

```
/* Maxima computer algebra system */

/* symbolic constants */

[true, false, unknown, inf, minf, ind,
 und, %e, %i, %pi, %phi, %gamma];

/* programming keywords */

if a then b elseif c then d else f;
for x:1 thru 10 step 2 do print(x);
for z:-2 while z < 0 do print(z);
for m:0 unless m > 10 do print(m);
for x in [1, 2, 3] do print(x);
foo and bar or not baz;

/* built-in variables */

[_, __, %, %%, linel, simp, dispflag,
 stringdisp, lispdisp, %edispflag];

/* built-in functions */

[sin, cosh, exp, atan2, sqrt, log, struve_h,
 sublist_indices, read_array];

/* user-defined symbols */

[foo, ?bar, baz%, quux_mumble_blurf];

/* symbols using Unicode characters */

[Љ, Щ, щ, Ӄ, ЩЩЩ, ӃӃЉЉщ];

/* numbers */

ibase : 18 $
[0, 1234, 1234., 0abcdefgh];
reset (ibase) $
[.54321, 3.21e+0, 12.34B56];

/* strings */

s1 : "\"now\" is";
s2 : "the 'time' for all good men";
print (s1, s2, "to come to the aid",
  "of their country");

/* expressions */

foo (x, y, z) :=
  if x > 1 + y
    then z - x*y
  elseif y <= 100!
    then x/(y + z)^2
  else z - y . x . y;
```
