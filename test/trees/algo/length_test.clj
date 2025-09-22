(ns trees.algo.length-test
  (:require [clojure.test :refer [deftest is]]
            [trees.test-utils.mock :as mock]
            [trees.algo.length :as sut]))

(deftest ratio-test
  (let [length-fn (sut/scale 32 0.5)]
    ;; Assuming mock/loc-trunk-only-1 is depth 1, next level is depth 2, etc.
    (is (= 32.0 (length-fn mock/loc-trunk-only-1)) 
        "Trunk should have full length")
    (is (= 16.0 (length-fn mock/loc-one-branch-2)) 
        "First branch should be half length")))
