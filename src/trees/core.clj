(ns trees.core
  (:require [quil.core :as q]
            [trees.tree :as tree]))


(def width 600)
(def height 600)

(defn downrightify-point
  "Translates a point from origin bottom left to origin top left"
  [[x y]]
  [x (- y)])

(defn setup []
  (q/background 0xDD)
  (let [tree (tree/grow 200 90 {:another-child? tree/right-angle-child?
                                :branch-angle   tree/right-angle-angle
                                :branch-length  tree/halving-branches-length
                                :children?      tree/smallest-branch-32-children?})]
    (q/with-translation [(/ width 2) height]
      (doseq [branch (tree-seq :children :children tree)]
        (q/line (downrightify-point (:start branch)) 
                (downrightify-point (:end branch)))))))

(defn draw []
  nil)

(q/defsketch trees
  :title "Fractal Trees"
  :size  [width height]
  :setup setup
  :draw  draw)