(ns dev.draw
  (:require
   [quil.core :as q]
   [trees.algo.angle :as angle]
   [trees.algo.children :as children]
   [trees.algo.combine :as combine]
   [trees.algo.jitter :as jitter]
   [trees.algo.length :as length]
   [trees.tree :as tree]
   [dev.examples :as examples]))

(def width 600)
(def height 600)

(defn downrightify-point [[x y]]
  [x (- y)])

(defn draw-tree [tree]
  (doseq [branch (tree-seq map? :children tree)]
    (when (:end branch)
      (q/line (downrightify-point (:start branch))
              (downrightify-point (:end branch))))))

(defn show-tree
  "Launch a Quil sketch to draw the given tree."
  [tree]
  (q/defsketch dev-tree
    :title "Fractal Tree"
    :size  [width height]
    :setup (fn []
             (q/background 0xDD)
             (q/with-translation [(/ width 2) height]
               (draw-tree tree)))
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
  (show-tree (tree/grow {:add-child?     (combine/with :and
                                                       (children/count<= 6)
                                                       (children/depth<= 4))
                         :branch-angle   (tree/with-vertical-trunk
                                           (combine/with +
                                                         (angle/regularly-spaced 120 6)
                                                         (angle/offset -5)
                                                         (jitter/even 10)))
                         :branch-length  (length/scale 200 1/2)}))
  (show-tree (tree/grow (examples/lopsided-spiral)))
  (show-tree (tree/grow (examples/binary-symmetric)))
  (show-tree (tree/grow (examples/radial-fan)))
  )