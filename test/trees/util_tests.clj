(ns trees.util-tests
  (:require [clojure.test :refer [deftest is]]
            [trees.util :as sut]
            [trees.test-utils.mock :as mock]))

(deftest testing-has-childer?
  (is (= false (sut/has-children? mock/loc-trunk-only-1))
      "Trunk of trunk only tree should have no children")
  (is (= true (sut/has-children? mock/loc-one-branch-1))
      "Trunk of tree with branches should have children")
  (is (= false (sut/has-children? mock/loc-one-branch-2))
      "Leaf of tree with branches should have no children"))

(deftest testing-depth
  (is (= 1 (sut/depth mock/loc-trunk-only-1))
      "Depth of trunk in trunk only tree should be 1")
  (is (= 1 (sut/depth mock/loc-two-branches-1))
      "Depth of trunk in trunk+branches should be 1")
  (is (= 2 (sut/depth mock/loc-two-branches-3))
      "Depth of leaf in trunk+branches should be 2"))

(comment
  (sut/depth mock/loc-one-branch-1)
  )