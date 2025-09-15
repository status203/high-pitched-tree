(ns trees.algo.length)

(defn scaled-branch-length
  "Returns a fn that scales eacah branch to `scale` of its parent's length"
  [scale]
  (fn branch-length [parent _siblings]
    (* (:length parent) scale)))

