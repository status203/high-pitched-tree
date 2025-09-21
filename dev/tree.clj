(ns dev.tree
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.zip :as z]

   [trees.algo.angle :as angle]
   [trees.algo.children :as children]
   [trees.algo.length :as length]
   [trees.tree :as tree]
   [trees.util :as u]))


;; Simple binomial tree with depth 2 and spread of 90°
(tree/grow {:children?     (children/enumerated-depth-children? 2)
            :branch-length (length/depth-decay-length 100 0.7)
            :branch-angle  (angle/with-vertical-trunk
                             (angle/enumerated-spread-angle 90 2 0))
            :add-child?    (children/enumerated-branches-child? 2)})

