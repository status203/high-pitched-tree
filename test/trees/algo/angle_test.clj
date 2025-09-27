(ns trees.algo.angle-test
  (:require [clojure.test :refer [deftest is]]
            [trees.test-utils.mock :as mock]
            [trees.algo.angle :as sut]))

(deftest regularly-spaced-tests
  (let [branch-angle (sut/regularly-spaced 90 2)]
    (is (= -45.0 (branch-angle mock/loc-one-branch-2))
        "Trunk's first branch should be -55°")
    (is (= 90.0 (branch-angle mock/loc-two-branches-3))
        "Trunk's second branch should be 90°")))