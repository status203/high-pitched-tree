(ns trees.algo.angle-tests
  (:require [clojure.test :refer [deftest is]]
            [trees.test-utils.mock :as mock]
            [trees.algo.angle :as sut]))

(deftest enumerated-spread-angle-tests
  (let [branch-angle (sut/enumerated-spread-angle 90 2 -10)]
    (is (= -55 (branch-angle mock/loc-trunk-only-1))
        "Trunk's first branch should be -55°")
    (is (= 35 (branch-angle mock/loc-one-branch-1))
        "Trunk's second branch should be 90°")))