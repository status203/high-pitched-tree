(ns trees.core
  (:require [quil.core :as q]
            [trees.tree :as tree]
            [trees.algo.angle :as angle]
            [trees.algo.children :as children]
            [trees.algo.length :as length]))

(def width 600)
(def height 600)

(defn downrightify-point
  "Translates a point from origin bottom left to origin top left"
  [[x y]]
  [x (- y)])

(def narrow-jittered-with-6-kids
  (tree/grow {:add-child?     (children/enumerated-branches-child? 6)
              :branch-angle   (angle/with-vertical-trunk
                                (angle/jittered-enumerated-spread-angle 120 6 0 10))
              :branch-length  (length/depth-decay-length 200 1/2)
              :children?      (children/enumerated-depth-children? 4)}))
(def depth-4-binomial
  (tree/grow {:children?     (children/enumerated-depth-children? 4)
              :branch-length (length/depth-decay-length 200 0.7)
              :branch-angle  (angle/with-vertical-trunk
                               (angle/enumerated-spread-angle 90 2 0))
              :add-child?    (children/enumerated-branches-child? 2)}))
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