# clojure-spec-seq

Creates a lazy seq of spec paths given a starting root spec.

## Usage

```clojure
(s/def :x/parent (s/keys :req [:x/name] :opt [:x/child]))
(s/def :x/child (s/keys :req [:x/name] :opt [:x/toy]))
(s/def :x/name string?)
(s/def :x/toy #{"Rubik's Cube" "Legos" "Xbox"})

(spec-seq :x/parent)
; => ({:spec-name :x/name, :depth 1, :path [:x/name], :req? true}
;     {:spec-name :x/child, :depth 1, :path [:x/child], :req? false}
;     {:spec-name :x/name, :depth 2, :path [:x/child :x/name], :req? true}
;     {:spec-name :x/toy, :depth 2, :path [:x/child :x/toy], :req? false})
```

This really comes in handy when we are dealing with specs that have cycles.

```clojure
(s/def :x/gossiper (s/keys :req [:x/rumor]))
(s/def :x/rumor (s/keys :req [:x/gossiper]))
```

In this case, we would generate a lazy infinite sequence. We can use things
like `take-while` to limit our sequence of child and descendant specs to those
less than a maximum depth

```clojure
(->> (spec-seq :x/gossiper)
     (take-while #(>= 3 (:depth %))))
; => ({:spec-name :x/rumor, :depth 1, :path [:x/rumor], :req? true}
;     {:spec-name :x/gossiper, :depth 2, :path [:x/rumor :x/gossiper], :req? true}
;     {:spec-name :x/rumor, :depth 3, :path [:x/rumor :x/gossiper :x/rumor], :req? true})
```

If you want to decorate the sequence members with more information (e.g., if
the spec corresponds to an integer; if the spec corresponds to a leaf node or
not), you can do so via a vector of middleware functions that you can pass as a
second argument to `spec-seq`. A middleware function is merely a decorator
function that takes the sequence member an a single argument and returns a new
sequence member.

```clojure
(let [middleware [#(assoc % :path-len (-> % :path count))]]
  (->> (spec-seq :x/gossiper)
       (take-while #(>= 3 (:depth %)) middleware)))
; => ({:spec-name :x/rumor, :depth 1, :path [:x/rumor], :req? true, :path-len 1}
;     {:spec-name :x/gossiper, :depth 2, :path [:x/rumor :x/gossiper], :req? true, :path-len 2}
;     {:spec-name :x/rumor, :depth 3, :path [:x/rumor :x/gossiper :x/rumor], :req? true, :path-len 3})
```

## License

The MIT License (MIT) Copyright © 2016 Lab79, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
