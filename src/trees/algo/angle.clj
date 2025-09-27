(ns trees.algo.angle
  (:require [clojure.zip :as z]
            [trees.util :as u]))

(defn regularly-spaced
  "Returns a branch-angle function.

  Takes a total spread (degrees) and number of branches, and returns a function
  that, given a zipper positioned on a placeholder child branch, computes the
  relative angle for that child. The zipper points to the child node whose angle
  is being calculated, not its parent. Branches are spaced evenly within the spread."
  ([spread n]
   (let [initial-angle (double (- (/ spread 2)))
         gap-angle     (double (/ spread (dec n)))]
     (fn branch-angle [loc]
       (if (z/right loc) gap-angle initial-angle)))))

(defn offset
  "Returns a branch-angle function that always returns the given constant offset.
   
  The zipper points to the placeholder child node whose angle is being calculated."
  [deg]
  (fn branch-angle [_loc] (double deg)))

(defn scale
  "Returns a branch-angle function.

  Given a zipper positioned on a placeholder child branch, computes the child's
  relative angle as:

      base-angle * (scale ^ (depth - 1))"
  [base-angle scale]
  (fn branch-angle [loc]
    (* base-angle
       (Math/pow scale (dec (u/depth loc))))))
