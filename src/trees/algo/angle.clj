(ns trees.algo.angle
  (:require [clojure.zip :as z]))

(defn regularly-spread
  "Takes a spread between first and last branches, and the number of total branches
   and returns a fn that will space the branches evenly within that space.
   offset is the degrees clockwise that the spread is rotated."
  ([spread n] (spread spread n 0))
  ([spread n offset]
   (let [initial-angle (+ (- (/ spread 2))
                          offset)
         gap-angle     (/ spread (dec n))]
     (fn branch-angle [loc]
       (if (z/right loc) gap-angle initial-angle)))))
