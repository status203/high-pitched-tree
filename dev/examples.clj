(ns dev.examples
  "Behaviour-named presets for `tree/grow`."
  (:require
   [trees.tree :as tree]
   [trees.util :as u]
   [trees.algo.angle :as angle]
   [trees.algo.children :as children]
   [trees.algo.combine :as combine]
   [trees.algo.indexed :as indexed]
   [trees.algo.jitter :as jitter]
   [trees.algo.length :as length]))

;; ------------------------------------------------------------
;; binary-symmetric — classic 2-child, tapering, symmetric
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
    (length/scale len0 len-decay)

    :add-child?
    (combine/with :and
                  (children/depth<= max-depth)
                  (children/length>= min-length)
                  (children/count<= 2))}))

;; ------------------------------------------------------------
;; radial-fan — one tier of k evenly spaced children
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
    (length/scale len0 len-decay)

    :add-child?
    (combine/with :and
                  (children/depth<= max-depth)
                  (children/count<= k))}))

(defn lopsided-spiral
  "Run `tree/grow` with a quickly-tweakable lopsided/spiral-ish setup.

   Options (all optional; sensible defaults match your example):
     :min-child-length   number   ;; threshold for children/length>= (default 5)
     :angles             vector   ;; per-child relative angles, iterator-style (default [-50 80])
     :ratios             vector   ;; per-child length factors (default [0.65 0.8])
     :trunk-length       number   ;; baseline trunk length via length/of-parent (default 200)
 
   Notes:
   - `children/count<= 2` is fixed here to keep a binary split.
   - `:angles` use the iterator convention:
        * first child: relative to parent
        * subsequent: relative to previous sibling
        * clockwise positive
   - `:ratios` are multiplied by the length of the parent branch)."
  ([]
   (lopsided-spiral {}))
  ([{:keys [leaf-length-limit angles ratios trunk-length]
     :or   {leaf-length-limit 5
            angles          [-50 65]
            ratios          [0.65 0.8]
            trunk-length    150}}]
   {:add-child?    (combine/with :and
                                 (children/count<= 2)
                                 (children/length>= leaf-length-limit))
    :branch-angle  (tree/with-vertical-trunk
                     (indexed/by-child angles))
    :branch-length (combine/with *
                                 (length/of-parent trunk-length)
                                 (indexed/by-child ratios))}
   ))
