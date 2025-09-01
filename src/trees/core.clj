(ns trees.core
  (:require [quil.core :as q]))

(defn setup []
  (q/background 0xDD))

(defn draw []
  nil)

(q/defsketch trees
  :title "Fractal Trees"
  :size  [600 600]
  :setup setup
  :draw  draw)