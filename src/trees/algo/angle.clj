(ns trees.algo.angle)

(defn enumerated-spread-angle
  "Takes a spread between first and last branches, and the number of total branches
   and returns a fn that will space the branches evenly within that space.
   offset is the degrees clockwise that the centre of the spread is rotated."
  ([spread n] (enumerated-spread-angle spread n 0))
  ([spread n offset]
   (let [initial-angle (+ (- (/ spread 2))
                          offset)
         gap-angle     (/ spread (dec n))]
     (fn branch-angle [_parents siblings]
       (if (empty? siblings) initial-angle gap-angle)))))
