(ns trees.tree
  (:require [clojure.zip :as z]
            [trees.util :as u]))

(defn base-angle
  "Takes a zipper to a new branch and returns the angle
   from which it should be calculating it's relative angle.
   i.e. previous sibling (if it has one), parent otherwise, default
   to 90 if it's the trunk."
  [loc]
  (if-let [base-loc (or (z/right loc) (z/up loc))]
    (-> base-loc z/node :abs-angle)
    90))

(defn- convert-angle
  "Takes a clockwise angle from West in degrees and converts it to an anti-clockwise
   angle from East in radians"
  [deg]
  (-> (- 180 deg)
      (mod 360)
      Math/toRadians))

(defn calc-end
  [[s1 s2] degs length]
  (let [rads (convert-angle degs)]
    [(+ s1 (* length (Math/cos rads)))
     (+ s2 (* length (Math/sin rads)))]))

(defn insert-placeholder-branch
  "Inserts a placeholder branch under the current location in the zipper, or
   at the root if nil, and returns the new zipper location
   .
   The placeholder branch knows only it's start point (the end of the parent 
   branch or [0 0] for the trunk), and has an empty children list.
   Returns the new zipper location."
  [loc]
  (if loc
    (-> loc
        (z/insert-child {:start (-> loc z/node :end)
                         :children  '()})
        z/down)
    (u/tree-zipper {:start [0 0]
                    :children  '()})))

(defn finalise-branch
  "Given a zipper location at a branch, populates the branch's
   :end, :rel-angle, :abs-angle and :length from the provided algorithm fns.
   Returns the updated zipper location."
  [loc {:keys [branch-angle branch-length]}]
  (let [node      (z/node loc)
        base      (base-angle loc)
        rel-angle (branch-angle loc)
        abs-angle (+ base rel-angle)
        length    (branch-length loc)
        start     (:start node)
        end       (calc-end start abs-angle length)]
    (z/replace loc
               (assoc node
                      :end       end
                      :rel-angle rel-angle
                      :abs-angle abs-angle
                      :length    length))))

(defn grow
  "opts (all are mandatory):
   
     :branch-angle - [child-zipper] -> degrees clockwise
     called to calculate the *relative* clockwise angle in degrees of a new child
     from a branch's last child or from the branch's angle if it's the first child.

     :branch-length - [child-zipper] -> length
     called to calculate the length of a new child branch

     :children? - [ptb-zipper] -> bool
     called to decide whether the potential parent-to-be branch should have any
     children

     :add-child? - [ptb-zipper] -> bool
     if a branch should have children then this is called each time to check
      whether another child should be added

   A branch is a map of 
     :start     [x y]
     :end       [x y]
     :rel-angle clockwise-degrees
     :abs-angle clockwise-degrees-from-West
     :length    length
     :children  (...branches)
   Note that, within :children, branches are stored from the most recent as
   z/insert-child -> z/down is more efficient.
    
   For now, assumes a single trunk (which is considered to have a depth of 1).
   The parent zipper of the trunk is nil and considered depth 0."
  ([{:keys [children?] :as opts}]
   (let [loc (-> (insert-placeholder-branch nil)
                 (finalise-branch opts))]
     (if (children? loc)
       (grow loc opts)
       (z/root loc))))
  ([loc {:keys [add-child? children?] :as opts}]
   (cond
     (add-child? loc)
     (let [loc' (-> loc
                   (insert-placeholder-branch)
                   (finalise-branch opts))]
       (if (children? loc')
         (recur loc' opts)
         (recur (z/up loc') opts)))
   
     (z/up loc) (recur (z/up loc) opts)
   
      :else (z/root loc))))
