(defproject lab79/clojure-spec-seq "0.1.0-SNAPSHOT"
  :description "Creates a lazy seq of spec paths given a starting root spec."
  :url "https://github.com/lab-79/clojure-spec-seq"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :source-paths ["src"]
  :test-paths ["test"]

  ; See https://github.com/technomancy/leiningen/issues/2173
  :monkeypatch-clojure-test false

  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/test.check "0.9.0"]
                 [lab79/clojure-spec-helpers "0.1.0-alpha1"]])
