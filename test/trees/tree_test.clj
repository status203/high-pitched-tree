(ns trees.tree-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.zip :as z]
            [trees.test-utils.mock :as mock]
            [trees.algo.angle :as angle]
            [trees.algo.children :as children]
            [trees.algo.combine :as combine]
            [trees.algo.curve :as curve]
            [trees.tree :as sut]))

(deftest base-angle-tests
  (is (= 90 (sut/base-angle mock/loc-trunk-only-1))
      "Base angle of trunk calculation should be 90")
  (is (= 90 (sut/base-angle mock/loc-one-branch-2))
      "Base angle of single branch should be 90")
  (is (= 45 (sut/base-angle mock/loc-two-branches-3))
      "Base angle of second branch should be 45"))

(def base-opts
  {:branch-angle (sut/with-vertical-trunk
                   (angle/regularly-spaced 90 2))
   :branch-length (curve/scale 100 0.7)
   :add-child? (combine/with :and
                               (children/count<= 2)
                               (children/depth<= 3))})

(deftest grow-tests
  (testing "growing a simple tree"
    (let [tree (sut/grow base-opts)]
      (is (= 7 (count (tree-seq map? :children tree))))))

  (testing "tree structure: children counts at each level"
    (let [tree (sut/grow base-opts)]
      (is (= 2 (count (:children tree))))
      (is (= 2 (-> tree :children first :children count)))
      (is (-> tree :children first :children first :children empty?))))

  (testing "branch lengths"
    (let [opts (assoc base-opts
                      :add-child? (combine/with :and
                                                  (children/count<= 2)
                                                  (children/depth<= 2)))
          tree (sut/grow opts)
          child-length (-> tree :children first :length)]
      (is (== 70.0 child-length))))

  (testing "branch angles"
    (let [opts (assoc base-opts
                      :add-child? (combine/with :and
                                                  (children/count<= 2)
                                                  (children/depth<= 2)))
          tree (sut/grow opts)
          angles (set (map :abs-angle (:children tree)))]
      (is (= #{45.0 135.0} angles))))

  (testing "single trunk (no children)"
    (let [opts {:branch-angle (constantly 0)
                :branch-length (constantly 0)
                :add-child? (constantly false)}
          tree (sut/grow opts)]
      (is (= 1 (count (tree-seq map? :children tree))))
      (is (empty? (:children tree))))))

(deftest finalise-branch-defaults
  (let [loc (sut/insert-placeholder-branch nil)
    loc2 (sut/finalise-branch loc {:branch-angle (constantly 0)
               :branch-length (constantly 0)})
    node (clojure.zip/node loc2)]
    ;; Using insert-placeholder and finalise should set defaults for width and colour
    (is (= 1 (:width node)) "Default width should be 1")
    (is (= [0 0 0 255] (:colour node)) "Default colour should be black")
    (is (= 0 (:length node)) "Length should be 0")
    (is (= 0 (:rel-angle node)) "Rel angle should be 0")))

(deftest finalise-branch-width-colour-fns
  (let [loc (sut/insert-placeholder-branch nil)
    opts {:branch-angle (constantly 0)
      :branch-length (constantly 10)
      :branch-width (constantly 3)
      :branch-colour (constantly "red")}
    loc2 (sut/finalise-branch loc opts)
    node (clojure.zip/node loc2)]
    (is (= 3 (:width node)) "Width function should be applied")
    (is (= "red" (:colour node)) "Colour function should be applied")
    (is (= 10 (:length node)) "Length should be set by branch-length")))