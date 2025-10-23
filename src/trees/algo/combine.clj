(ns trees.algo.combine
  (:require [clojure.zip :as z]))

(defmacro by
  "Define NAME as a value-dispatching fn of one arg (a zipper loc).

   Usage: (by name accessor-fn
            spec-1 handler-1
            spec-2 handler-2
            ...)

   The accessor-fn will be called with the loc to produce the value which is
   matched against the LHS specs (checked in order):
     - integer N        -> (= v N)
     - [lo hi]          -> range inclusive of lo, exclusive of hi: [lo hi)
                           (MUST be literal: lo integer; hi integer or :+inf)
     - predicate (fn)   -> called with loc; truthy => match
     - :else            -> catch-all

   Example:
     (by choose-angle trees.util/depth
       1         angle-a
       [2 4]     angle-b
       [5 :+inf] angle-c
       (fn [loc] (odd? (trees.util/depth loc))) angle-odd
       :else     angle-default)"
  [name accessor & pairs]
  (when (odd? (count pairs))
    (throw (ex-info "by: requires an even number of forms (spec/handler pairs)."
                    {:given (count pairs)})))
  (let [loc-sym (gensym "loc")
        v-sym   (gensym "v")
        pair-forms (partition 2 pairs)
        clause-forms
        (mapcat
         (fn [[spec handler]]
           (cond
             (integer? spec)
             [`(= ~v-sym ~spec) `(~handler ~loc-sym)]

             (and (vector? spec) (= 2 (count spec)))
             (let [[lo hi] spec]
               (cond
                 (and (integer? lo) (integer? hi))
                 [`(and (<= ~lo ~v-sym) (< ~v-sym ~hi)) `(~handler ~loc-sym)]

                 (and (integer? lo) (= hi :+inf))
                 [`(<= ~lo ~v-sym) `(~handler ~loc-sym)]

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
         pair-forms)
        has-else? (some #(= % :else) (take-nth 2 clause-forms))
        body (if has-else?
               `(cond ~@clause-forms)
               `(let [res# (cond ~@clause-forms :else ::no-match)]
                  (if (= res# ::no-match)
                    (throw (ex-info "No matching router clause and no :else provided."
                                    {:value ~v-sym}))
                    res#)))]
    `(def ~name
       (fn [~loc-sym]
         (let [~v-sym (~accessor ~loc-sym)]
           ~body)))))

(defmacro by-depth
  "Convenience wrapper for building routers based on branch depth."
  [name & pairs]
  `(by ~name trees.util/depth ~@pairs))

(defmacro by-length
  "Convenience wrapper for building routers based on branch :length."
  [name & pairs]
  `(by ~name (fn [~'loc] (:length (z/node ~'loc))) ~@pairs))

(defmacro by-width
  "Convenience wrapper for building routers based on branch :width."
  [name & pairs]
  `(by ~name (fn [~'loc] (:width (z/node ~'loc))) ~@pairs))
(defn with
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

