(ns trees.algo.length-test
  (:require [clojure.test :refer [deftest is]]
            [trees.test-utils.mock :as mock]
            [trees.algo.length :as sut]))

(deftest of-parent-test
  (let [length-fn (sut/of-parent 100)]
    (is (= 100.0 (length-fn mock/loc-trunk-only-1))
        "Trunk should have trunk length")
    (is (= 32.0 (length-fn mock/loc-one-branch-2))
        "Branch should have parent's length (mocked as 32.0)")))
