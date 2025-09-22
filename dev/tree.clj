(ns dev.tree
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.zip :as z]

   [trees.algo.angle :as angle]
   [trees.algo.children :as children]
   [trees.algo.combine :as combine]
   [trees.algo.length :as length]
   [trees.tree :as tree]
   [trees.util :as u]))


;; Simple binomial tree with depth 2 and spread of 90°
(tree/grow {:branch-length (length/ratio 100 0.7)
            :branch-angle  (tree/with-vertical-trunk
                             (angle/regularly-spaced 90 2))
            :add-child?    (combine/with :and
                                         (children/count<= 2)
                                         (children/depth<= 2))})

