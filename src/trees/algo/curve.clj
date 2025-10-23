(ns trees.algo.curve
  (:require [trees.util :as u]))

(defn ^:private depth-ratio [{:keys [depth max-depth]}]
  (double (if (pos? max-depth) (/ depth max-depth) 0.0)))

;; Curves: depth context -> scalar or absolute value
(defn power
  "Returns a depth-scaling function.

  Given a zipper `loc`, computes a value proportional to:

      base * ((1 - (depth/max-depth)) ^ exponent)

  where `exponent` > 1 accentuates decay with depth, and < 1 flattens it.
  Suitable for cases where deeper branches change more smoothly near the tips."
  [base exponent max-depth]
  (fn [loc]
    (let [d (u/depth loc)
          ratio (double (/ d max-depth))]
      (* base (Math/pow (- 1.0 ratio) exponent)))))


(defn linear
  "Returns a depth-scaling function.

  Interpolates linearly from `start` at depth 1 to `end` at `max-depth`:

      start + (end - start) * ((depth - 1) / (max-depth - 1))"
  [start end max-depth]
  (fn [loc]
    (let [d (dec (u/depth loc))
          t (double (/ d (max 1 (dec max-depth))))]
      (+ start (* (- end start) t)))))

(defn scale
  "Returns a multiplicative scaling function.

  For a branch at depth n:

      base * (factor ^ (depth - 1))

  A factor < 1 shrinks values with depth; > 1 enlarges them."
  [base factor]
  (fn [loc]
    (* base
       (Math/pow factor (dec (u/depth loc))))))

