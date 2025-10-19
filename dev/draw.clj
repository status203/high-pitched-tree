(ns dev.draw
  (:require
   [quil.core :as q]
 
   [trees.util :as util]))

;; Calculation prefixes
;; m - model. I.e. the tree
;; v - viewport
;; r - ratio e.g viewport width / model width - vw - mw
;; s - scaled box - the model after scaling
;; p - placement


(defn- clamp [lo x hi] (max lo (min x hi)))

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

(defn draw-tree
  "Draw TREE with:
   - X always centred then clamped to fit
   - Y always baseline (y=0 at bottom) then clamped to fit

   opts:
     :scale   :none (default) | :to-fit | :to-view | :contain | :cover
     :padding integer px (default 16)
     :debug   truthy → draw viewport & placed box outlines"
  [tree {:keys [width height scale padding debug bg] 
         :or   {width 600, height 600, padding 20, debug false,
                scale :none, bg 0xDD}}]
  (when tree
    (let [{:keys [min-x min-y max-x max-y]} (util/bounds tree)
          mw  (max 0.0 (- max-x min-x))
          mh  (max 0.0 (- max-y min-y))

          ;; viewport (inside padding)
          vl  padding
          vt  padding
          vw  (- width  (* 2 padding))
          vh  (- height (* 2 padding))

          ;; guard degenerates to avoid div-by-zero
          mw* (if (pos? mw) mw 1.0)
          mh* (if (pos? mh) mh 1.0)
          rx  (/ vw mw*)
          ry  (/ vh mh*)

          s   (choose-scale scale rx ry)
          sw  (* s mw)
          sh  (* s mh)

          px  (place-x vl vw sw)
          py  (place-y vt vh sh s min-y)]

      (q/background bg)

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

      (doseq [b (tree-seq map? :children tree)]
        (when-let [[x2 y2] (:end b)]
          (let [[x1 y1] (:start b)]
            (q/line x1 y1 x2 y2))))
      (q/pop-matrix))))
