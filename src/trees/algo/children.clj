(ns trees.algo.children)

(defn enumerated-branches-children?
  "Returns a fn that returns true until n children have been produced"
  [n]
  (fn children? [_parents siblings]
    (boolean (> n (count siblings)))))


(defn grow-until-drop-below-length-children?
  "Returns a fn that returns true unless a branch is at or below given length"
  [length]
  (fn children? [_parent _siblings branch]
    (> (:length branch) length)))