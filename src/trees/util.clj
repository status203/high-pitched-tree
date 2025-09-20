(ns trees.util
  (:require [clojure.zip :as z]))


(defn- tree-zipper-children
  [node]
  (-> node :children seq))

(defn- tree-zipper-make-node
  [node children]
  (assoc node :children children))

(def tree-zipper (partial z/zipper map? tree-zipper-children tree-zipper-make-node))

(defn lift-to-loc-fn
 "Lifts an operator * to work on the results of fns that take a zipper loc.
  The lifted fn takes a zipper loc and applies the operator to the results
  of applying each fn to that loc."
 ([op] (fn [loc] (op)))
 ([op f] (fn [loc] (op (f loc))))
 ([op f g] (fn [loc] (op (f loc) (g loc))))
 ([op f g & rst]
  (let [fns (list* f g rst)]
    (fn [loc] (reduce op (map #(% loc) fns))))))

(defn jitter
  "Return a random fp number between -a and a"
  [a]
  (- (* 2 a (rand))
     a))

(defn jitter-loc
  "Return a fn that takes a zipper loc, ignores it, and returns a random fp 
   number between -a and a"
  [a]
  (fn [_] (jitter a)))

(defn has-children?
  "Returns whether the branch represented by the zipper has any children"
  [loc]
  (boolean (-> loc z/down)))

(defn depth
  "Returns the depth of a zipper"
  ([loc] (depth loc 1))
  ([loc acc]
   (if-let [parent (z/up loc)]
     (recur parent (inc acc))
     acc)))

(defn stochastic-round
  "Stochastically round x to an integer.
   Rounds down with probability 1 - frac(x) and up with probability frac(x),
   where frac(x) = x - floor(x). Works for negatives.
   Examples:
     (stochastic-round 2.3) ; 2 with ~0.7 prob, 3 with ~0.3
     (stochastic-round -2.3) ; -2 with ~0.7, -3 with ~0.3
     (stochastic-round 5.0) ; always 5"
  [x]
  (let [f    (Math/floor x)
        frac (- x f)]
    (long (if (< (rand) frac) (inc f) f))))

