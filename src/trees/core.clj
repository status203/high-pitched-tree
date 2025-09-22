(ns trees.core
  (:require [quil.core :as q]
            [trees.tree :as tree]
            [trees.algo.angle :as angle]
            [trees.algo.children :as children]
            [trees.algo.combine :as combine]
            [trees.algo.length :as length]
            [trees.algo.jitter :as jitter]))

(def width 600)
(def height 600)

(defn downrightify-point
  "Translates a point from origin bottom left to origin top left"
  [[x y]]
  [x (- y)])

(def narrow-jittered-with-6-kids
  (tree/grow {:add-child?     (combine/with :and
                                            (children/count<= 6)
                                            (children/depth<= 4))
              :branch-angle   (tree/with-vertical-trunk
                                (combine/with +
                                              (angle/regularly-spaced 120 6)
                                              (angle/offset -5)
                                              (jitter/even 10)))
              :branch-length  (length/ratio 200 1/2)}))
(def depth-4-binomial
  (tree/grow {:add-child?     (combine/with :and
                                              (children/count<= 2)
                                              (children/depth<= 4))
              :branch-length (length/ratio 200 0.7)
              :branch-angle  (tree/with-vertical-trunk
                               (angle/regularly-spaced 90 2))}))
(defn setup []
  (q/background 0xDD)
  (q/with-translation [(/ width 2) height]
    (doseq [branch (tree-seq :children :children narrow-jittered-with-6-kids)]
      (q/line (downrightify-point (:start branch)) 
              (downrightify-point (:end branch))))))

(defn draw []
  nil)

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(q/defsketch trees
  :title "Fractal Trees"
  :size  [width height]
  :setup setup
  :draw  draw)