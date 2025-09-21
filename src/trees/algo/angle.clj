(ns trees.algo.angle
  (:require [clojure.zip :as z]
            [trees.util :as u]))

;; Note that base angle is 90 for trunk
(def vertical-trunk (constantly 0))

(defn with-vertical-trunk
  "Returns a fn that uses vertical-trunk for depth 1, otherwise uses the 
   provided angle-fn"
  [angle-fn]
  (fn branch-angle? [loc]
    (if (= 1 (u/depth loc))
      (vertical-trunk loc)
      (angle-fn loc))))

(defn enumerated-spread-angle
  "Takes a spread between first and last branches, and the number of total branches
   and returns a fn that will space the branches evenly within that space.
   offset is the degrees clockwise that the centre of the spread is rotated."
  ([spread n] (enumerated-spread-angle spread n 0))
  ([spread n offset]
   (let [initial-angle (+ (- (/ spread 2))
                          offset)
         gap-angle     (/ spread (dec n))]
     (fn branch-angle [loc]
       (if (z/right loc) gap-angle initial-angle)))))

(defn jittered-enumerated-spread-angle
  "Returns a fn similar to enumerated-spread-angle but adds a random amount 
   between ±jitter to each angle"
  [spread n offset jitter]
  (u/lift-to-loc-fn +
                 (enumerated-spread-angle spread n offset)
                 (u/jitter-loc jitter)))