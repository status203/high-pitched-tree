(ns trees.algo.length
  (:require [trees.util :as u]))

(defn scale
  "Returns a branch-length function.
  
  Given a zipper positioned on a branch node where a child will be added,
  computes the child's length as:
  
      base-length * (scale ^ (depth - 1))
  
  Thus, each successive branch is a fixed multiple (scale) of its parent’s
  length. A scale < 1 produces shrinking branches, while a scale > 1
  produces growing branches."
  [initial-length scale]
  (fn branch-length [loc]
    (* initial-length
       (Math/pow scale (dec (u/depth loc))))))
