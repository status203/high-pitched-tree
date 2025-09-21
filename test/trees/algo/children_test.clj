(ns trees.algo.children-test
  (:require [clojure.test :refer [deftest is]]
            [trees.test-utils.mock :as mock]
            [trees.algo.children :as sut]))

(deftest count<=-tests
  (let [add-child? (sut/count<= 2)]
    (is (= true (add-child? mock/loc-trunk-only-1))
        "Trunk only should have a child added")
    (is (= true (add-child? mock/loc-one-branch-1))
        "Trunk with one child should have another child")
    (is (= false (add-child? mock/loc-two-branches-1))
        "Full trunk shouldn't have another child")))

(deftest depth<=-tests
  (let [children? (sut/depth<= 2)]
    (is (= true (children? mock/loc-trunk-only-1))
        "Trunk should have children")
    (is (= false (children? mock/loc-one-branch-2))
        "Depth 2 branch should not have children")))

(deftest length>=-tests
  (let [children?15 (sut/length>= 15)
        children?16 (sut/length>= 16)
        children?17 (sut/length>= 17)]
    (is (= true (children?15 mock/loc-trunk-only-1))
        "Trunk length >= 15 - should have children")
    (is (= true (children?15 mock/loc-one-branch-2))
        "Branch length >= 15 - should have children")
    
    (is (= true (children?16 mock/loc-trunk-only-1))
        "Trunk length >= 16 - should have children")
    (is (= true (children?15 mock/loc-one-branch-2))
        "Branch length >= 16 - should have children")
    
    (is (= true (children?17 mock/loc-trunk-only-1))
        "Trunk length >= 17 - should have children")
    (is (= true (children?15 mock/loc-one-branch-2))
        "Branch length < 17 - should not have children")))