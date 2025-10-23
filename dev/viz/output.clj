(ns viz.output
  (:require
   [quil.core :as q]

   [viz.draw :as draw]
   [examples]

   [trees.tree :as tree]))

(def default-opts
  {:width   750
   :height  600
   :padding 20
   :scale   :contain
   :bg      0xDD
   :debug   false})

(defn show-tree
  "Launch a Quil sketch to draw the given tree."
  [opts tree]
  (let [opts' (merge default-opts opts)
        {:keys [width height]} opts']
    (q/defsketch dev-tree
      :title    "Fractal Tree"
      :size     [width height]
      :features [:no-loop]
      :setup    (fn [] nil)
      :draw     (fn []
                  (draw/draw-tree tree opts')))))

;; Example usage:
;; (show-tree
;;   (tree/grow {:branch-length (curve/scale 100 0.7)
;;               :branch-angle  (tree/with-vertical-trunk
;;                                (angle/regularly-spaced 90 2))
;;               :add-child?    (combine/with :and
;;                                            (children/count<= 2)
;;                                            (children/depth<= 2))}))

(comment
  (def show (partial show-tree {}))
  (show (tree/grow (examples/lopsided-spiral)))
  (show (tree/grow (examples/binary-symmetric)))
  (show (tree/grow (examples/radial-fan)))
  (show (tree/grow examples/jittered-and-offset))
)