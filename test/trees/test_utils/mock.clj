(ns trees.test-utils.mock
  (:require [clojure.zip :as z]
            [trees.util :as u]))

;;
;; branch nodes
;;

;; trunk
(def branch1 {:start [0 0]
              :end       [32 32]
              :rel-angle 0
              :abs-angle 90
              :length    32
              :children  '()})

;;
(def branch2 {:start [0 0]
              :end       [20 20]
              :rel-angle -45
              :abs-angle 45
              :length    16
              :children  '()})

(def branch3 {:start [10 10]
              :end       [30 30]
              :rel-angle 90
              :abs-angle 45
              :length    16
              :children  '()})

;;
;; Trees
;;

(def trunk-only branch1)
(def one-branch (-> trunk-only u/tree-zipper (z/insert-child branch2) z/root))
(def two-branches (-> one-branch u/tree-zipper (z/insert-child branch3) z/root))

;;
;; Zipper locations
;; loc-<tree>-<location/branch-node-number>
;; Note that conj'ing to a list means the most recent branch is the leftmost
;; zipper location
;;

(def loc-trunk-only-1 "trunk, duh!" (-> trunk-only u/tree-zipper))
(def loc-one-branch-1 "trunk" (-> one-branch u/tree-zipper))
(def loc-one-branch-2 "second layer, sole branch" (-> one-branch u/tree-zipper z/down))
(def loc-two-branches-1 "trunk" (-> two-branches u/tree-zipper))
(def loc-two-branches-2 "second layer, first branch" (-> two-branches u/tree-zipper z/down z/right))
(def loc-two-branches-3 "second layer, second branch" (-> two-branches u/tree-zipper z/down))

