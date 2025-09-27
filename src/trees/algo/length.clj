(ns trees.algo.length
  (:require [trees.util :as u]
            [clojure.zip :as z]))

(defn of-parent
  "Returns a branch-length function.
   
   Given a zipper positioned on a placeholder child branch, returns the 
   parent's length, or trunk-length if we're dealing with the trunk"
  [trunk-length]
  (fn branch-length [loc]
    (if-let [parent-loc (z/up loc)]
      (-> parent-loc z/node :length double)
      (double trunk-length))))

(defn scale
  "Returns a branch-length function.

  Given a zipper positioned on a placeholder child branch, computes the child's
  length as:

      base-length * (scale ^ (depth - 1))

  A scale < 1 produces shrinking branches, while a scale > 1 produces growing 
  branches."
  [base-length scale]
  (fn branch-length [loc]
    (* base-length
       (Math/pow scale (dec (u/depth loc))))))
