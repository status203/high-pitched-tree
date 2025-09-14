(ns trees.calc-test
  (:require [clojure.test :refer [deftest testing is]]
            [trees.tree :as sut]))


(deftest right-angle-sibling?
  (testing "Add a first branch?"
    (is (= true (sut/right-angle-child? :na []))))
  (testing "Add an additional branch?"
    (is (= true (sut/right-angle-child? :na [:b1]))))
  (testing "No more branches"
    (is (= false (sut/right-angle-child? :na [:b1 :b2])))))

(deftest right-angle-angle-tests
  (testing "First branch"
    (is (= -45 (sut/right-angle-angle :na []))))
  (testing "Second branch"
    (is (= 90 (sut/right-angle-angle :na [:b1])))))

(deftest halving-branches-length-tests
  (is (= 0.2 (sut/halving-branches-length {:length 0.4} :na))))

(deftest smallest-branch-32-children?-tests
  (testing "Above branching limit"
    (is (= true (sut/smallest-branch-32-children? :na :na {:length 33}))))
  (testing "On branching limit"
    (is (= false (sut/smallest-branch-32-children? :na :na {:length 32}))))
  (testing "Below branching limit"
    (is (= false (sut/smallest-branch-32-children? :na :na {:length 31.999})))))

(deftest base-angle-tests
  (testing "branch with no children"
    (is (= 90 (sut/base-angle {:angle 90} []))))
  (testing "branch with multiple children"
    (is (= 135 (sut/base-angle {:angle 90} [{:angle -45} {:angle 90}])))))