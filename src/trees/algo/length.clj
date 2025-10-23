(ns trees.algo.length
  (:require [clojure.zip :as z]))

(defn of-parent
  "Returns a branch-length function.
   
   Given a zipper positioned on a placeholder child branch, returns the 
   parent's length, or trunk-length if we're dealing with the trunk"
  [trunk-length]
  (fn branch-length [loc]
    (if-let [parent-loc (z/up loc)]
      (-> parent-loc z/node :length double)
      (double trunk-length))))

(defn scale
  [_base-length _scale]
  (throw (ex-info "scale moved to trees.algo.curve/scale")))
