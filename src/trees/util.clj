(ns trees.util
  (:require [clojure.zip :as z]))


(defn- tree-zipper-children
  [node]
  (-> node :children seq))

(defn- tree-zipper-make-node
  [node children]
  (assoc node :children children))

(def tree-zipper (partial z/zipper map? tree-zipper-children tree-zipper-make-node))

(defn combine-with 
  "Lifts an operator [X] to work on the results [zipper]->X
   
   Use :and/:or for predicates as `and`/`or` are macros"
  [op & fs]
  (cond
    ;; AND: stop on first falsey
    (= op :and)
    (fn [loc]
      (loop [fs fs]
        (if (empty? fs)
          true
          (let [v ((first fs) loc)]
            (if v
              (recur (rest fs))
              false)))))

    ;; OR: stop on first truthy, return that truthy value
    (= op :or)
    (fn [loc]
      (loop [fs fs]
        (if (empty? fs)
          false
          (let [v ((first fs) loc)]
            (if v
              v
              (recur (rest fs)))))))

    ;; Any plain function op (e.g. +, max) → evaluate all
    (fn? op)
    (let [j (apply juxt fs)]
      (fn [loc] 
        (apply op (j loc))))

    :else
    (throw (ex-info "Unsupported op" {:op op}))))

(defn has-children?
  "Returns whether the branch represented by the zipper has any children"
  [loc]
  (boolean (-> loc z/down)))

(defn depth
  "Returns the depth of a zipper"
  ([loc] (depth loc 0))
  ([loc acc]
   (if loc
     (recur (z/up loc) (inc acc))
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

(defmacro defdepth-router
  "Define NAME as a depth-dispatching fn of one arg (a zipper loc).

   LHS spec forms (checked in order):
     - integer N        -> (= d N)
     - [lo hi]          -> inclusive;
                           (MUST be literal: lo integer; hi integer or :+inf)
     - predicate (fn)   -> called with loc; truthy => match
     - :else            -> catch-all

   Example:
     (defdepth-router choose-angle
       1         angle-a
       [2 4]     angle-b
       [5 :+inf] angle-c
       (fn [loc] (odd? (trees.util/depth loc))) angle-odd
       :else     angle-default)"
  [name & pairs]
  (when (odd? (count pairs))
    (throw (ex-info "defdepth-router requires an even number of forms (spec/handler pairs)."
                    {:given (count pairs)})))
  (let [loc-sym (gensym "loc")
        d-sym   (gensym "d")
        clauses (for [[spec handler] (partition 2 pairs)]
                  (cond
                    (integer? spec)
                    [`(= ~d-sym ~spec) `(~handler ~loc-sym)]

                    (and (vector? spec) (= 2 (count spec)))
                    (let [[lo hi] spec]
                      (cond
                        (and (integer? lo) (integer? hi))
                        [`(<= ~lo ~d-sym ~hi) `(~handler ~loc-sym)]
                        (and (integer? lo) (= hi :+inf))
                        [`(<= ~lo ~d-sym) `(~handler ~loc-sym)]
                        :else
                        (throw (ex-info "Range must be literal: [lo hi] with lo integer and hi integer or :+inf."
                                        {:spec spec}))))

                    (= spec :else)
                    [:else `(~handler ~loc-sym)]

                    :else
                    [`(let [p# ~spec]
                        (when-not (fn? p#)
                          (throw (ex-info "Predicate spec did not resolve to a function."
                                          {:spec '~spec})))
                        (boolean (p# ~loc-sym)))
                     `(~handler ~loc-sym)]))
        has-else? (some #(= (first %) :else) clauses)
        cond-forms (mapcat identity clauses)
        body (if has-else?
               `(cond ~@cond-forms)
               `(let [res# (cond ~@cond-forms :else ::no-match)]
                  (if (= res# ::no-match)
                    (throw (ex-info "No matching depth-router clause and no :else provided."
                                    {:depth ~d-sym}))
                    res#)))]
    `(def ~name
       (fn [~loc-sym]
         (let [~d-sym (depth ~loc-sym)]
           ~body)))))



