(ns lab79.clojure-spec-seq
  (:require #?(:clj  [clojure.spec :as s]
               :cljs [cljs.spec :as s])
    [lab79.clojure-spec-helpers :refer [extract-spec-keys is-keys-spec?]]))

(s/def ::spec-name keyword?)
(s/def ::depth (s/and int? (complement neg?)))
(s/def ::path (s/coll-of ::spec-name :kind vector?))
(s/def ::req? boolean?)
(s/def ::spec-seq-member (s/keys :req-un [::spec-name ::depth ::path ::req?]))
(s/def ::middleware-fn (s/fspec :args (s/cat :spec-seq-member ::spec-seq-member)
                                :ret ::spec-seq-member))

(defn- my-mapcat
  "Lazy mapcat with no memory leaks.
  See http://clojurian.blogspot.com/2012/11/beware-of-mapcat.html"
  [f coll]
  (lazy-seq
    (if (not-empty coll)
      (concat
        (f (first coll))
        (my-mapcat f (rest coll))))))

(s/fdef spec-seq
        :args (s/or :arity-1 (s/cat :spec-name ::spec-name)
                    :arity-2 (s/cat :spec-name ::spec-name
                                    :middleware (s/coll-of ::middleware-fn :kind vector?))
                    :arity-4 (s/cat :spec-name ::spec-name
                                    :middleware (s/coll-of ::middleware-fn :kind vector?)
                                    :depth ::depth
                                    :path ::path))
        :ret seq?)
(defn spec-seq
  "Given a spec that may define a map or collection, this returns a lazy sequence of all child specs (i.e., spec names
  that are `:req` or `:opt`) and all subsequent descendant specs -- along with corresponding (tree) depth, spec path
  to the descendant spec, and whether the given spec is a required or optional key of its immediate parent."
  ([spec-name]
   (spec-seq spec-name []))
  ([spec-name middleware]
   (spec-seq spec-name middleware 0 []))
  ([spec-name middleware depth path]
   (lazy-seq
     (when (is-keys-spec? spec-name)
       (let [{:keys [req opt]} (extract-spec-keys spec-name)
             next-depth (inc depth)]
         (concat
           (map (fn [spec-name']
                  (reduce (fn [val middleware-fn] (middleware-fn val))
                          {:spec-name spec-name'
                           :depth     next-depth
                           :path      (conj path spec-name')
                           :req?      true}
                          middleware))
                req)
           (map (fn [spec-name']
                  (reduce (fn [val middleware-fn] (middleware-fn val))
                          {:spec-name spec-name'
                           :depth     next-depth
                           :path      (conj path spec-name')
                           :req?      false}
                          middleware))
                opt)
           (my-mapcat #(spec-seq % middleware next-depth (conj path %))
                      (concat req opt))))))))