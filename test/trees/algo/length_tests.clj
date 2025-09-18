(ns trees.algo.length-tests
  (:require [clojure.test :refer [deftest is]]
            [trees.test-utils.mock :as mock]
            [trees.algo.length :as sut]))

(deftest scaled-branch-length-test
  (let [branch-length? (sut/scaled-branch-length 1/2)]
    (is (= 16 (branch-length? mock/loc-trunk-only-1)))))