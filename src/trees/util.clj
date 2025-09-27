(ns trees.util
  (:require [clojure.zip :as z]))


(defn- tree-zipper-children
  [node]
  (-> node :children seq))

(defn- tree-zipper-make-node
  [node children]
  (assoc node :children children))

(defn first-child?
  "Returns whether the branch represented by the zipper is the first child of 
   its parent"
  [loc]
  (if loc (not (z/right loc)) true))

(def tree-zipper (partial z/zipper map? tree-zipper-children tree-zipper-make-node))

(defn has-children?
  "Returns whether the branch represented by the zipper has any children"
  [loc]
  (boolean (-> loc z/down)))

(defn depth
  "Returns the depth of a zipper"
  [loc]
  (if loc
    (-> loc z/path count inc)
     0))

(defn child-index
 "Return the (1-based) index of a zipper representing a branch amongst that
  branch's siblings"
 [loc]
 (-> loc z/rights count inc))

(defn stochastic-round
  "Stochastically round x to an integer.
   Rounds down with probability 1 - frac(x) and up with probability frac(x),
   where frac(x) = x - floor(x). Works for negatives.
   Examples:
     (stochastic-round 2.3) ; 2 with ~0.7 prob, 3 with ~0.3
     (stochastic-round -2.3) ; -2 with ~0.7, -3 with ~0.3
     (stochastic-round 5.0) ; always 5"
  [x]
  (let [f    (Math/floor x)
        frac (- x f)]
    (long (if (< (rand) frac) (inc f) f))))




