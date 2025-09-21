(ns trees.algo.length
  (:require [trees.util :as u]))

(defn ratio
  "Returns a branch-length function.
  
  Given a zipper positioned on a branch node where a child will be added,
  computes the child's length as:
  
      base-length * (ratio ^ (depth - 1))
  
  Thus, each successive branch is a fixed multiple (ratio) of its parent’s
  length. A ratio < 1 produces shrinking branches, while a ratio > 1
  produces growing branches."
  [initial-length ratio]
  (fn branch-length [loc]
    (* initial-length
       (Math/pow ratio (dec (u/depth loc))))))
