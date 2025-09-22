(ns trees.algo.angle
  (:require [clojure.zip :as z]))

(defn regularly-spaced
  "Takes a spread between first and last branches, and the number of total branches
   and returns a fn that will space the branches evenly within that space."
  ([spread n]
   (let [initial-angle (- (/ spread 2))
         gap-angle     (/ spread (dec n))]
     (fn branch-angle [loc]
       (if (z/right loc) gap-angle initial-angle)))))

(defn offset
  "Returns an angle fn that returns a constant amount"
  [deg]
  (fn branch-angle [_loc] deg))
