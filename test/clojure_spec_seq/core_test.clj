(ns clojure-spec-seq.core-test
  (:require [clojure.test :refer :all]
            [lab79.clojure-spec-seq :refer :all]
            [clojure.spec :as s]
            [clojure.spec.test :as stest]))

(doseq [x (stest/enumerate-namespace 'lab79.clojure-spec-seq)]
  (stest/instrument x))

(deftest test-spec-seq
  (testing "Non keys specs and collection specs of key specs should return empty"
    (s/def ::integer int?)
    (is (empty? (spec-seq ::integer))))
  (testing "Should return a finite sequence for a spec with no cycles"
    (s/def ::no-cycles-root (s/keys :req [::no-cycles-middle]))
    (s/def ::no-cycles-middle (s/keys :req [::integer]))
    (s/def ::integer int?)
    (is (= (spec-seq ::no-cycles-root)
           '({:spec-name :clojure-spec-seq.core-test/no-cycles-middle,
              :depth 1,
              :path [:clojure-spec-seq.core-test/no-cycles-middle],
              :req? true}
              {:spec-name :clojure-spec-seq.core-test/integer,
               :depth 2,
               :path [:clojure-spec-seq.core-test/no-cycles-middle :clojure-spec-seq.core-test/integer],
               :req? true}))))
  (testing "Should return an infinite sequence for a spec with cycles"
    (s/def :x/sample-map (s/keys :req [:x/sample-map-2] :opt [:x/sample-opt]))
    (s/def :x/sample-map-2 (s/keys :opt [:x/sample-map]))
    (s/def :x/sample-opt string?)
    (testing "involving a keys spec"
      (is (= (take-while #(>= 3 (:depth %)) (spec-seq :x/sample-map)))
          '({:spec-name :x/sample-map-2, :depth 1, :path [:x/sample-map-2], :req? true}
             {:spec-name :x/sample-opt, :depth 1, :path [:x/sample-opt], :req? false}
             {:spec-name :x/sample-map, :depth 2, :path [:x/sample-map-2 :x/sample-map], :req? false}
             {:spec-name :x/sample-map-2, :depth 3, :path [:x/sample-map-2 :x/sample-map :x/sample-map-2], :req? true}
             {:spec-name :x/sample-opt, :depth 3, :path [:x/sample-map-2 :x/sample-map :x/sample-opt], :req? false})))
    (testing "involving a collection spec of keys spec"
      (s/def :x/sample-coll-of-maps (s/coll-of :x/sample-map))
      (is (= (take-while #(>= 3 (:depth %)) (spec-seq :x/sample-coll-of-maps)))
          '({:spec-name :x/sample-map-2, :depth 1, :path [:x/sample-map-2], :req? true}
             {:spec-name :x/sample-opt, :depth 1, :path [:x/sample-opt], :req? false}
             {:spec-name :x/sample-map, :depth 2, :path [:x/sample-map-2 :x/sample-map], :req? false}
             {:spec-name :x/sample-map-2, :depth 3, :path [:x/sample-map-2 :x/sample-map :x/sample-map-2], :req? true}
             {:spec-name :x/sample-opt, :depth 3, :path [:x/sample-map-2 :x/sample-map :x/sample-opt], :req? false})))
    (testing "middleware should decorate sequence members"
      (let [middleware [#(assoc % :path-len (-> % :path count))]]
        (is (= (take-while #(>= 3 (:depth %)) (spec-seq :x/sample-map middleware))
               '({:spec-name :x/sample-map-2, :depth 1, :path [:x/sample-map-2], :req? true, :path-len 1}
                  {:spec-name :x/sample-opt, :depth 1, :path [:x/sample-opt], :req? false, :path-len 1}
                  {:spec-name :x/sample-map, :depth 2, :path [:x/sample-map-2 :x/sample-map], :req? false, :path-len 2}
                  {:spec-name :x/sample-map-2, :depth 3, :path [:x/sample-map-2 :x/sample-map :x/sample-map-2], :req? true, :path-len 3}
                  {:spec-name :x/sample-opt, :depth 3, :path [:x/sample-map-2 :x/sample-map :x/sample-opt], :req? false, :path-len 3})))))))