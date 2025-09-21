(ns trees.algo.length
  (:require [trees.util :as u]))

(defn depth-decay-length
  "Returns a function that, given a zipper positioned on a branch node
   where a child will be added, computes the new child branch's length
   by scaling the provided length by scale^(depth-1)."
  [length scale]
  (fn branch-length [loc]
    (* length
       (Math/pow scale (dec (u/depth loc))))))
