(ns trees.algo.jitter)

(defn even
  "Return (fn [loc] ...) that samples a uniform integer in [min, max] 
   (inclusive). The one-arg version is between [-n, n]"
  ([n] (even (- n) n))
  ([min max]
   (let [min (long min)
         n   (inc (- (long max) min))] ;; count of values
     (fn [_loc] (+ min (rand-int n))))))


(comment
  (let [f (even -2 5)]
    (take 30 (repeatedly (partial f nil))))
  (let [f (even 4)]
    (take 30 (repeatedly (partial f nil))))
  )