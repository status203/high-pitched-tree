(ns trees.algo.length
  (:require [clojure.zip :as z]))

(defn scaled-branch-length
  "Returns a function that, given a zipper positioned on a branch node
   where a child will be added, computes the new child branch's length
   by scaling the parent's :length by `scale`."
  [scale]
  (fn branch-length [loc]
    (* (-> loc z/node :length) scale)))

