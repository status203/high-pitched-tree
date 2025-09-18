(ns trees.tree-tests
  (:require [clojure.test :refer [deftest is testing]]
            [trees.tree :as tree]
            [trees.algo.angle :as angle]
            [trees.algo.children :as children]
            [trees.algo.length :as length]))

(def base-opts
  {:branch-angle (angle/enumerated-spread-angle 90 2 -10)
   :branch-length (length/scaled-branch-length 0.7)
   :add-child? (children/enumerated-branches-child? 2)
   :children? (children/enumerated-depth-children? 3)})

(deftest grow-tests
  (testing "growing a simple tree"
    (let [tree (tree/grow 50 90 base-opts)]
      (is (= 7 (count (tree-seq map? :children tree))))))

  (testing "tree structure: children counts at each level"
    (let [tree (tree/grow 50 90 base-opts)]
      (is (= 2 (count (:children tree))))
      (is (= 2 (-> tree :children first :children count)))
      (is (-> tree :children first :children first :children empty?))))

  (testing "branch lengths"
    (let [opts (assoc base-opts
                 :branch-length (length/scaled-branch-length 0.7)
                 :children? (children/enumerated-depth-children? 2))
          tree (tree/grow 100 90 opts)
          child-length (-> tree :children first :length)]
      (is (== 70.0 child-length))))

  (testing "branch angles"
    (let [opts (assoc base-opts
                 :branch-length (length/scaled-branch-length 1)
                 :children? (children/enumerated-depth-children? 2))
          tree (tree/grow 50 90 opts)
          angles (set (map :abs-angle (:children tree)))]
      (is (= #{35 125} angles))))

  (testing "single trunk (no children)"
    (let [opts {:branch-angle (constantly 0)
                :branch-length (constantly 0)
                :add-child? (constantly false)
                :children? (constantly false)}
          tree (tree/grow 50 90 opts)]
      (is (= 1 (count (tree-seq map? :children tree))))
      (is (empty? (:children tree))))))