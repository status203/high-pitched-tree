(ns trees.algo.curve-test
  (:require [clojure.test :refer [deftest is]]
            [trees.test-utils.mock :as mock]
            [trees.algo.curve :as sut]))

(deftest power-test
  ;; power: base * ((1 - (depth/max-depth)) ^ exponent)
  (let [f (sut/power 10 2 4)]
    ;; depths: mock/loc-trunk-only-1 => depth 1 -> 10 * (0.75^2) = 5.625
    ;;        mock/loc-one-branch-2 => depth 2 -> 10 * (0.5^2) = 2.5
    (is (= 5.625 (f mock/loc-trunk-only-1)) "Power at depth 1 should be 5.625")
    (is (= 2.5 (f mock/loc-one-branch-2)) "Power at depth 2 should be 2.5")))

(deftest linear-test
  ;; linear interpolation start -> end across depths
  (let [f (sut/linear 0 10 3)]
    ;; At depth 1 -> start (0); at max-depth -> end (10)
    (is (= 0.0 (f mock/loc-trunk-only-1)) "Depth 1 should be start")
    ;; mock/loc-two-branches-3 is depth 2; with max-depth 3 value should be halfway -> 5.0
    (is (= 5.0 (f mock/loc-two-branches-3)) "Depth 2 should be halfway (5.0)")))

(deftest scale-test
  (let [f (sut/scale 2 0.5)]
    ;; depth 1 -> 2 * (0.5 ^ 0) = 2
    (is (= 2.0 (f mock/loc-trunk-only-1)) "Depth 1 should be base")
    ;; depth 2 -> 2 * (0.5 ^ 1) = 1
    (is (= 1.0 (f mock/loc-one-branch-2)) "Depth 2 should be base * factor")))
