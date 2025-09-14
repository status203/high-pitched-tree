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
  (let [tree (tree/grow 200 90 {:another-child? (tree/enumerated-branches-children? 7)
                                :branch-angle   (tree/enumerated-spread-angle 150 7)
                                :branch-length  (tree/scaled-branch-length 1/2)
                                :children?      (tree/grow-until-drop-below-length-children? 32)})]
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