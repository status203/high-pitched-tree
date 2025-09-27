(ns trees.util-test
  (:require [clojure.test :refer [deftest is]]
            [trees.util :as sut]
            [trees.test-utils.mock :as mock]))

(deftest has-children?-tests
  (is (= false (sut/has-children? mock/loc-trunk-only-1))
      "Trunk of trunk only tree should have no children")
  (is (= true (sut/has-children? mock/loc-one-branch-1))
      "Trunk of tree with branches should have children")
  (is (= false (sut/has-children? mock/loc-one-branch-2))
      "Leaf of tree with branches should have no children"))

(deftest depth-tests
  (is (= 0 (sut/depth nil))
      "Depth of nil should be 0")
  (is (= 1 (sut/depth mock/loc-trunk-only-1))
      "Depth of trunk in trunk only tree should be 1")
  (is (= 1 (sut/depth mock/loc-two-branches-1))
      "Depth of trunk in trunk+branches should be 1")
  (is (= 2 (sut/depth mock/loc-two-branches-3))
      "Depth of leaf in trunk+branches should be 2"))

(deftest first-child?-tests
  (is (= true (sut/first-child? mock/loc-one-branch-2))
      "First branch should be first child")
  (is (= true (sut/first-child? mock/loc-two-branches-2))
      "First of two branches should be first child")
  (is (= false (sut/first-child? mock/loc-two-branches-3))
      "Second of two branches should not be first child")
  (is (= true (sut/first-child? mock/loc-trunk-only-1))
      "Trunk should be considered first child (or root)")
  (is (= true (sut/first-child? nil))
      "Nil location should be considered first child"))

(deftest child-index-tests
  (is (= 1 (sut/child-index mock/loc-trunk-only-1))
      "Trunk should be first (and only) child")
  (is (= 1 (sut/child-index mock/loc-one-branch-2))
      "First branch should have index 1")
  (is (= 1 (sut/child-index mock/loc-two-branches-2))
      "First of two branches should have index 1")
  (is (= 2 (sut/child-index mock/loc-two-branches-3))
      "Second of two branches should have index 2"))
