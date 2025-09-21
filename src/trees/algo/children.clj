(ns trees.algo.children
  (:require [clojure.zip :as z]
            [trees.util :as u]))

(defn enumerated-branches-child?
  "Returns a fn that takes a zipper and returns true if the node has fewer than
   n children"
  [n]
  (fn add-child? [loc]
    (boolean (> n (count (-> loc z/node :children))))))

(defn enumerated-depth-children?
  "Returns a fn that takes a zipper and returns true unless the branch it
   represents has the given depth"
  [depth]
  (fn children? [loc]
    (< (u/depth loc) depth)))

(defn grow-until-drop-below-length-children?
  "Returns a fn that takes a zipper and returns true unless the branch it 
   represents is at or below the given length"
  [length]
  (fn children? [loc]
    (> (-> loc z/node :length) length)))