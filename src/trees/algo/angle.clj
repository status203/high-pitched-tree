(ns trees.algo.angle
  (:require [trees.util :as u]))

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
       (if (u/has-children? loc) gap-angle initial-angle)))))

;; todo: generalise (let no one else's work evade your eyes)
(defn jittered-enumerated-spread-angle
  "Returns a fn similar to enumerated-spread-angle but adds a random amount 
   between ±jitter to each angle"
  [spread n offset jitter] 
  (let [base-fn (enumerated-spread-angle spread n offset)]
    (fn branch-angle [loc]
      (+ (base-fn loc)
         (u/jitter jitter)))))