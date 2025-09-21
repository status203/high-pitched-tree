(ns trees.util-test
  (:require [clojure.test :refer [deftest testing is]]
            [trees.util :as sut]
            [trees.test-utils.mock :as mock]
            [clojure.zip :as z]))

(deftest has-childer?-tests
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

(deftest lift-loc-fn-tests
  (is (= 0 ((sut/lift-to-loc-fn +) mock/loc-one-branch-1))
      "lift-loc-fn with no fns should return op identity")
  (is (= 1 ((sut/lift-to-loc-fn + sut/depth) mock/loc-one-branch-1))
      "lift-loc-fn with one fn should work")
  (is (= 2 ((sut/lift-to-loc-fn + sut/depth sut/depth) mock/loc-one-branch-1))
      "lift-loc-fn with two fns should work")
  (is (= 12 ((sut/lift-to-loc-fn + sut/depth sut/depth sut/depth sut/depth sut/depth sut/depth)
            mock/loc-one-branch-2))
      "lift-loc-fn with many fns should work"))


;; 
;; Depth router
;;


(defn- fake-depth [loc] (::depth (z/node loc)))
(defn- loc-with-depth [n]
  (z/zipper (constantly false) (constantly nil) (fn [n _] n) {::depth n}))

(deftest defdepth-router-tests
  (testing "dispatch ordering"
    (with-redefs [sut/depth fake-depth]
      (letfn [(A [_] :A) (B [_] :B) (C [_] :C) (O [_] :O) (D [_] :D)]
        #_{:clj-kondo/ignore [:inline-def]}
        (sut/defdepth-router choose-angle
          1         A
          [2 4]     B
          [5 :+inf] C
          (fn [loc] (odd? (sut/depth loc))) O
          :else     D)
        (is (= :A (choose-angle (loc-with-depth 1))))
        (is (= :B (choose-angle (loc-with-depth 2))))
        (is (= :B (choose-angle (loc-with-depth 4))))
        (is (= :C (choose-angle (loc-with-depth 7))))
        (is (= :B (choose-angle (loc-with-depth 3))))
        (is (= :D (choose-angle (loc-with-depth 0)))))))

  (testing "predicate must resolve to fn"
    (with-redefs [sut/depth fake-depth]
      #_{:clj-kondo/ignore [:inline-def]}
      (sut/defdepth-router p
        :not-a-fn   (fn [_] :X)
        :else       (fn [_] :Y))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Predicate spec did not resolve to a function"
           (p (loc-with-depth 9)))))))