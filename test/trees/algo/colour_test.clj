(ns trees.algo.colour-test
  (:require [clojure.test :refer [deftest is testing]]
            [trees.algo.colour :as sut]))

(deftest parsing-and-normalisation
  (testing "hex->rgba parses common forms"
    (is (= [255 0 0 255] (sut/hex->rgba "#F00")))
    (is (= [17 34 51 255] (sut/hex->rgba "#112233")))
    (is (= [17 34 51 170] (sut/hex->rgba "#112233AA")))
    (is (= [17 34 51 170] (sut/hex->rgba 0xAA112233))))

  (testing "ensure-rgba normalises vectors and hex"
    (is (= [1 2 3 255] (sut/ensure-rgba [1 2 3])))
    (is (= [1 2 3 4]   (sut/ensure-rgba [1 2 3 4])))
    (is (= [255 255 255 255] (sut/ensure-rgba "#FFFFFF"))))

  (testing "rgba->hex8 formats uppercase with alpha"
    (is (= "#112233AA" (sut/rgba->hex8 [17 34 51 170])))))

(deftest linear-interpolation
  (let [start [0 0 0 255]
        finish [255 255 255 255]
        f (sut/linear start finish identity 0 10)]
    (testing "endpoints"
      (is (= start (f 0)))
      (is (= finish (f 10))))
    (testing "midpoint ~ 50%"
      (is (= [128 128 128 255] (f 5))))
    (testing "clamping below/above domain"
      (is (= start (f -100)))
      (is (= finish (f 100)))))

  (testing "reversed domain (lo > hi) maps correctly"
    (let [start [0 0 0 255]
          finish [200 100 50 255]
          f (sut/linear start finish identity 10 0)]
      (is (= start (f 10)))
      (is (= finish (f 0)))
      (is (= [100 50 25 255] (f 5))))))

(deftest gamma-interpolation
  (let [start [0 0 0 255]
        finish [200 0 0 255]
        f (sut/gamma start finish identity 0 1 2.0)]
    (testing "t=0 → start; t=1 → finish"
      (is (= start (f 0.0)))
      (is (= finish (f 1.0))))
    (testing "t=0.5 with gamma=2 → 25% of range"
      (is (= [50 0 0 255] (f 0.5))))))

(deftest smoothstep-interpolation
  (let [start [0 0 0 255]
        finish [255 0 255 255]
        f (sut/smoothstep start finish identity 0 1)]
    (testing "endpoints"
      (is (= start (f 0.0)))
      (is (= finish (f 1.0))))
    (testing "midpoint is ~50%"
      (is (= [128 0 128 255] (f 0.5))))))

(deftest zero-width-domain
  (testing "lo==hi → always start colour"
    (let [start [10 20 30 255]
          finish [200 200 200 255]
          f (sut/linear start finish identity 5 5)]
      (is (= start (f 5)))
      (is (= start (f 0)))
      (is (= start (f 10))))))
(ns trees.algo.colour-test)