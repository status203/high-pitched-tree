(ns trees.tree)

(defn enumerated-branches-children?
  "Returns a fn that returns true until n children have been produced"
  [n]
  (fn children? [_parents siblings]
    (boolean (> n (count siblings)))))

(defn enumerated-spread-angle
  "Takes a spread between first and last branches, and the number of total branches
   and returns a fn that will space the branches evenly within that space"
  [spread n]
  (let [initial-angle (- (/ spread 2))
        gap-angle     (/ spread (dec n))]
    (fn branch-angle [_parents siblings]
      (if (empty? siblings) initial-angle gap-angle))))

(defn scaled-branch-length
  "Returns a fn that scales each branch to `scale` of its parent's length"
  [scale]
  (fn branch-length [parent _siblings]
    (* (:length parent) scale)))

(defn grow-until-drop-below-length-children?
  "Returns a fn that returns true unless a branch is at or below given length"
  [length]
  (fn children? [_parent _siblings branch]
    (> (:length branch) length)))

(def right-angle-child? (enumerated-branches-children? 2))
(def right-angle-angle (enumerated-spread-angle 90 2))
(def halving-branches-length (scaled-branch-length 1/2))
(def smallest-branch-32-children? (grow-until-drop-below-length-children? 32))

(defn base-angle
  "Takes a (growing) branch and it's chldren to date and returns the angle
   from which the next child's branch should be calculated.
   
   Note that branch angles are calculated clockwise"
  [parent siblings]
  (println :got-here)
  (if-let [sibling-angle (->> siblings last :abs-angle)]
    sibling-angle
    (:abs-angle parent)))

(defn- convert-angle
  "Takes a clockwise angle from West in degrees and converts it to an anti-clockwise
   angle from East in radians"
  [deg]
  (-> deg
      (mod 360)
      Math/toRadians))

(defn calc-end
  [[s1 s2] degs length]
  (let [rads (convert-angle degs)]
    [(+ s1 (* length (Math/cos rads)))
     (+ s2 (* length (Math/sin rads)))]))

(defn grow
  "opts (all are mandatory)
     :branch-angle - [parents siblingls] -> degrees clockwise
     called to calculate the relative angle from a branch's last sibling or from 
     the parents angle if it's the first child.

     :branch-length - [parents branch] -> length
     called to calculate the length of the branch

     :another-child? - [parents siblings] -> bool
     called to decide whether to add another branch to siblings

     :children? - [parents siblings branch] -> bool
     called to decide whether the branch should have any children

   A branch is a map of 
     :start     [x y]
     :end       [x y]
     :rel-angle clockwise-degrees
     :abs-angle clockwise-degrees-from-West
     :length    length
     :parent    branch
     :children  [...branches]
    
   For now, assumes a single trunk (which is considered to be branch level 0)"
  ([trunk-length trunk-angle opts]
   (let [trunk {:start [0 0]
                :end       (calc-end [0 0] trunk-angle trunk-length)
                :rel-angle trunk-angle
                :abs-angle trunk-angle
                :length    trunk-length
                :parent    nil
                :children  nil}]
     (grow trunk opts)))
  ([parent {:keys [another-child? branch-angle branch-length children?] :as opts}]
   (loop [siblings []]
     (if (another-child? parent siblings)
       (let [rel-angle (branch-angle parent siblings)
             abs-angle (+ (base-angle parent siblings) rel-angle)
             length    (branch-length parent siblings)
             start     (:end parent)
             end       (calc-end start abs-angle length)
             base      {:start     start
                        :end       end
                        :rel-angle rel-angle
                        :abs-angle abs-angle
                        :length    length
                        :parent    parent
                        :children  nil}
             child     (if (children? parent siblings base) (grow base opts) base)]
         (recur (conj siblings child)))
       (assoc parent :children siblings)))))

(comment
  (grow 200 90 {:another-child? right-angle-child?
                :branch-angle   right-angle-angle
                :branch-length  halving-branches-length
                :children?      smallest-branch-32-children?}))
