(ns trees.algo.children
  (:require [clojure.zip :as z]
            [trees.util :as u]))

(defn count<=
  "Returns a fn that takes a zipper and returns true if the node has fewer than
   n children"
  [n]
  (fn add-child? [loc]
    (boolean (> n (count (-> loc z/node :children))))))

(defn depth<=
  "Returns a fn that takes a zipper and returns true unless the branch it
   represents has the given max depth"
  [max-depth]
  (fn children? [loc]
    (< (u/depth loc) max-depth)))

(defn length>=
  "Returns a fn that takes a zipper and returns true unless the branch it 
   represents is at or below the given length"
  [length]
  (fn children? [loc]
    (> (-> loc z/node :length) length)))