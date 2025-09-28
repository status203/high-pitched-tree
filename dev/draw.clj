(ns dev.draw
  (:require
   [quil.core :as q]
   [trees.algo.angle :as angle]
   [trees.algo.children :as children]
   [trees.algo.combine :as combine]
   [trees.algo.jitter :as jitter]
   [trees.algo.length :as length]
   [trees.tree :as tree]
   [trees.util :as util]
   [dev.examples :as examples]))

(def width 600)
(def height 600)

;; Calculation prefixes
;; m - model. I.e. the tree
;; v - viewport
;; r - ratio e.g viewport width / model width - vw - mw
;; s - scaled box - the model after scaling
;; p - placement

(defn- clamp [lo x hi] (max lo (min x hi)))

(defn- contain-scale [mw mh vw vh]
  (if (and (pos? mw) (pos? mh))
    (min (/ vw mw) (/ vh mh))
    1.0))

(defn choose-scale
  "rx = Rw/mw, ry = Rh/mh (Rect/Tree). strategy is one of
   :none | :to-fit | :to-view | :contain | :cover (optional).
   Note: ratios will be >= 1 if an axis fits, < 1 if overflowing"
  [strategy rx ry]
  (let [r (min rx ry)]
    (case strategy
      :none     1.0
      :to-fit   (min 1.0 r)
      :to-view  (max 1.0 r)
      :contain  r
      1.0)))

(defn- place-x 
  "On the x axis, centre, then clamp to [0, Rw - sw]"
  [vl vw sw]
  (let [cx (/ (- vw sw) 2.0)]
    (+ vl (clamp 0 cx (- vw sw)))))

(defn- place-y
  "On the Y axis, baseline then clamp: put model y=0 at viewport bottom if it fits,
   otherwise lift to keep everything visible."
  [Rt Rh sh s min-y]
  (let [py0 (- (+ Rt Rh) sh (* s min-y))]
    (clamp Rt py0 (- (+ Rt Rh) sh))))
(defn- place-y.bak
  "On the Y axis, baseline (map y=0 to bottom), then clamp to [Rt, Rt+Rh - sh]
   From Yscreen = py + sh - s*(y - min-y),
   set y=0 → Rt+Rh  ⇒ py0 = Rt+Rh - sh + s*min-y"
  [vt vh sh s min-y]
  (let [py0 (+ vt vh (- sh) (* s min-y))]
    (clamp vt py0 (+ vt vh (- sh)))))

(defn draw-tree
  "Draw TREE with:
   - X always centred then clamped to fit
   - Y always baseline (y=0 at bottom) then clamped to fit

   opts:
     :scale   :none (default) | :to-fit | :to-view | :contain | :cover
     :padding integer px (default 16)
     :debug   truthy → draw viewport & placed box outlines"
  [tree {:keys [scale padding debug]}]
  (when tree
    (let [{:keys [min-x min-y max-x max-y]} (util/bounds tree)
          mw  (max 0.0 (- max-x min-x))
          mh  (max 0.0 (- max-y min-y))
          pad (or padding 16)

          ;; viewport (inside padding)
          vl  pad
          vt  pad
          vw  (- (q/width)  (* 2 pad))
          vh  (- (q/height) (* 2 pad))

          ;; guard degenerates to avoid div-by-zero
          mw* (if (pos? mw) mw 1.0)
          mh* (if (pos? mh) mh 1.0)
          rx  (/ vw mw*)
          ry  (/ vh mh*)

          s   (choose-scale (or scale :none) rx ry)
          sw  (* s mw)
          sh  (* s mh)

          px  (place-x vl vw sw)
          py  (place-y vt vh sh s min-y)]

      (when debug
        (q/no-fill)
        (q/stroke 200) (q/rect vl vt vw vh)   ; viewport
        (q/stroke 150) (q/rect px py sw sh)) ; placed box
      
      (q/push-matrix)
      ;; move to top-left of placed box, but set baseline for y-flip
      (q/translate px (+ py sh))
      ;; uniform scale + flip Y (Quil y-down → model y-up)
      (q/scale s (- s))
      ;; put model bbox min corner at origin
      (q/translate (- min-x) (- min-y))

      ;; draw branches in model coords
      (doseq [b (tree-seq map? :children tree)]
        (when-let [[x2 y2] (:end b)]
          (let [[x1 y1] (:start b)]
            (q/line x1 y1 x2 y2))))
      (q/pop-matrix))))

(defn show-tree
  "Launch a Quil sketch to draw the given tree."
  [opts tree]
  (q/defsketch dev-tree
    :title "Fractal Tree"
    :size  [width height]
    :setup (fn []
             (q/background 0xDD)
             (draw-tree tree opts))
    :draw (fn [] nil)))

;; Example usage:
;; (show-tree
;;   (tree/grow {:branch-length (length/scale 100 0.7)
;;               :branch-angle  (tree/with-vertical-trunk
;;                                (angle/regularly-spaced 90 2))
;;               :add-child?    (combine/with :and
;;                                            (children/count<= 2)
;;                                            (children/depth<= 2))}))

(comment
  (def opts {:scale :to-view})
  (def show (partial show-tree opts))
  (show (tree/grow {:add-child?     (combine/with :and
                                                       (children/count<= 6)
                                                       (children/depth<= 4))
                         :branch-angle   (tree/with-vertical-trunk
                                           (combine/with +
                                                         (angle/regularly-spaced 120 6)
                                                         (angle/offset -5)
                                                         (jitter/even 10)))
                         :branch-length  (length/scale 200 1/2)}))
  (show (tree/grow (examples/lopsided-spiral)))
  (show (tree/grow (examples/binary-symmetric)))
  (show (tree/grow (examples/radial-fan)))
  )