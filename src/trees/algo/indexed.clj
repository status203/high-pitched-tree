(ns trees.algo.indexed
  "Algos that pick values from a vector using tree indices.

   - Tree depths and sibling indices are **1-based**, i.e. 1 is the trunk.
   - Vector lookup is still 0-based as in Clojure, so index N in the tree maps
     to element (N - 1) in the vector.
   - Indices beyond the vector’s length are clamped to the last element.
   - Empty vectors are invalid and will throw."
  (:require [clojure.zip :as z]
            [trees.util :as u]))

(defn- nth-clamped
  "Return v[idx-1] with 1-based idx, clamped to [1 (count v)]."
  [v idx]
  (let [n (count v)]
    (cond
      (zero? n) (throw (ex-info "Empty vector supplied to index picker" {:idx idx}))
      (<= idx 1) (v 0)
      (>= idx n) (v (dec n))
      :else      (v (dec idx)))))

(defn by-depth
  "Return an algo fn `[loc] -> value` that chooses from `vs` by 1-based depth.

   Examples:
     (def length* (by-depth [100 70 49]))
     (length* trunk-loc)      ;; depth=1 -> 100
     (length* child-loc)      ;; depth=2 -> 70
     (length* deep-loc)       ;; depth>=4 -> 49 (clamped)."
  [vs]
  (fn [loc]
    (nth-clamped vs (u/depth loc))))

(defn by-child
  "Return an algo fn `[loc] -> value` that chooses from `vs` by 1-based sibling index.

   Examples:
     (def angle* (by-siblings [10 6 4]))
     (angle* first-child-loc)  ;; index=1 -> 10
     (angle* second-child-loc) ;; index=2 -> 6
     (angle* fifth-child-loc)  ;; index=5 -> 4 (clamped)."
  [vs]
  (fn [loc]
    (nth-clamped vs (u/child-index loc))))
