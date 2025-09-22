(ns dev.examples
  "Behaviour-named presets for `tree/grow`."
  (:require
   [clojure.zip :as z]
   [trees.tree :as tree]
   [trees.util :as u]
   [trees.algo.angle :as angle]
   [trees.algo.length :as len]
   [trees.algo.children :as child]
   [trees.algo.jitter :as jitter]
   [trees.algo.combine :as combine]))

;; ------------------------------------------------------------
;; 1) binary-symmetric — classic 2-child, tapering, symmetric
;; ------------------------------------------------------------
(defn binary-symmetric
  "Classic symmetric binary split.
   Keys (defaults): {:angle0 32.0 :angle-decay 0.92 :len0 100.0 :len-decay 0.72
                     :max-depth 12 :min-length 2.0}"
  ([] (binary-symmetric {}))
  ([{:keys [angle0 angle-decay len0 len-decay max-depth min-length]
     :or   {angle0 32.0, angle-decay 0.92,
            len0 100.0, len-decay 0.72,
            max-depth 12, min-length 2.0}}]
   {:branch-angle
    (tree/with-vertical-trunk
      (combine/with *                     ;; unit fan × depth-scaled spread
                    (angle/regularly-spaced 2 2)     
                    (angle/scale angle0 angle-decay)))

    :branch-length
    (len/scale len0 len-decay)

    :add-child?
    (combine/with :and
                  (child/depth<= max-depth)
                  (child/length>= min-length)
                  (child/count<= 2))}))

;; ------------------------------------------------------------
;; 2) radial-fan — one tier of k evenly spaced children
;; ------------------------------------------------------------
(defn radial-fan
  "Evenly spaced k-ray fan at each node.
   Keys: {:k 5 :spread0 160.0 :spread-decay 0.80 :len0 100.0 :len-decay 0.80
          :max-depth 6}"
  ([] (radial-fan {}))
  ([{:keys [k spread0 spread-decay len0 len-decay max-depth]
     :or   {k 5 spread0 160.0 spread-decay 0.80
            len0 100.0 len-decay 0.80
            max-depth 6}}]
   {:branch-angle
    (tree/with-vertical-trunk
      (combine/with *
                    (angle/regularly-spaced 1 k)
                    (angle/scale spread0 spread-decay)))

    :branch-length
    (len/scale len0 len-decay)

    :add-child?
    (combine/with :and
                  (child/depth<= max-depth)
                  (child/count<= k))}))

