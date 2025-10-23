(ns trees.algo.combine-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.zip :as z]
            [trees.test-utils.mock :as mock]
            [trees.util :as u]
            [trees.algo.combine :as sut]))

(deftest with-tests
  (is (= 1 ((sut/with + u/depth) mock/loc-one-branch-1))
      "combine-with + with one fn should work")
  (is (= 2 ((sut/with + u/depth u/depth) mock/loc-one-branch-1))
      "combine-with + with two fns should work")
  (is (= 12 ((sut/with + u/depth u/depth u/depth u/depth u/depth u/depth)
             mock/loc-one-branch-2))
      "combine-with + with many fns should work")

  ;; :and tests
  (is (= true ((sut/with :and (constantly true) (constantly true)) mock/loc-trunk-only-1))
      "combine-with :and returns true if all are true")
  (is (= false ((sut/with :and (constantly true) (constantly false)) mock/loc-trunk-only-1))
      "combine-with :and returns false if any are false")
  (is (= false ((sut/with :and (constantly false) (constantly true)) mock/loc-trunk-only-1))
      "combine-with :and short-circuits on first false")

  ;; :or tests
  (is (= true ((sut/with :or (constantly false) (constantly true)) mock/loc-trunk-only-1))
      "combine-with :or returns true if any are true")
  (is (= false ((sut/with :or (constantly false) (constantly false)) mock/loc-trunk-only-1))
      "combine-with :or returns false if all are false")
  (is (= true ((sut/with :or (constantly true) (constantly false)) mock/loc-trunk-only-1))
      "combine-with :or short-circuits on first true"))

;; 
;; Depth router
;;

(defn- fake-depth [loc] (::depth (z/node loc)))

(defn- loc-with-depth [n]
  (z/zipper (constantly false) (constantly nil) (fn [n _] n) {::depth n}))

(deftest by-depth-tests
  (testing "dispatch ordering"
    (with-redefs [u/depth fake-depth]
      (letfn [(A [_] :A) (B [_] :B) (C [_] :C) (O [_] :O) (D [_] :D)]
        #_{:clj-kondo/ignore [:inline-def]}
        (sut/by-depth choose-angle
                      1         A
                      [2 5]     B
                      [5 :+inf] C
                      (fn [loc] (odd? (u/depth loc))) O
                      :else     D)
        (is (= :A (choose-angle (loc-with-depth 1))))
        (is (= :B (choose-angle (loc-with-depth 2))))
        (is (= :B (choose-angle (loc-with-depth 4))))
        (is (= :C (choose-angle (loc-with-depth 7))))
        (is (= :B (choose-angle (loc-with-depth 3))))
        (is (= :D (choose-angle (loc-with-depth 0)))))))

  (testing "predicate must resolve to fn"
    (with-redefs [u/depth fake-depth]
      #_{:clj-kondo/ignore [:inline-def]}
      (sut/by-depth p
        :not-a-fn   (fn [_] :X)
        :else       (fn [_] :Y))
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo
           #"Predicate spec did not resolve to a function"
           (p (loc-with-depth 9)))))))

;;
;; by-length and by-width
;;

(defn- loc-with-node [m]
  (z/zipper (constantly false) (constantly nil) (fn [n _] n) m))

(deftest by-length-tests
  (testing "dispatch by :length with [lo hi) semantics"
    (letfn [(L1 [_] :L1) (L2 [_] :L2) (Lbig [_] :LB) (P [_] :P) (E [_] :E)]
      #_{:clj-kondo/ignore [:inline-def]}
      (sut/by-length choose-by-length
                     8         L1
                     [10 20]   L2
                     [20 :+inf] Lbig
                     (fn [loc] (odd? (:length (z/node loc)))) P
                     :else     E)
      (is (= :L1 (choose-by-length (loc-with-node {:length 8}))))
      (is (= :L2 (choose-by-length (loc-with-node {:length 10}))))
      (is (= :L2 (choose-by-length (loc-with-node {:length 19}))))
      (is (= :LB (choose-by-length (loc-with-node {:length 20}))))
      (is (= :LB (choose-by-length (loc-with-node {:length 25}))))
      (is (= :P  (choose-by-length (loc-with-node {:length 7}))))))

  (testing "predicate must resolve to fn for by-length"
    #_{:clj-kondo/ignore [:inline-def]}
    (sut/by-length bl-bad
      :not-a-fn   (fn [_] :X)
      :else       (fn [_] :Y))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Predicate spec did not resolve to a function"
         (bl-bad (loc-with-node {:length 13}))))))

(deftest by-width-tests
  (testing "dispatch by :width with [lo hi) semantics"
    (letfn [(W1 [_] :W1) (W2 [_] :W2) (Wbig [_] :WB) (P [_] :P) (E [_] :E)]
      #_{:clj-kondo/ignore [:inline-def]}
      (sut/by-width choose-by-width
                    1         W1
                    [2 4]     W2
                    [4 :+inf] Wbig
                    (fn [loc] (odd? (:width (z/node loc)))) P
                    :else     E)
      (is (= :W1 (choose-by-width (loc-with-node {:width 1}))))
      (is (= :W2 (choose-by-width (loc-with-node {:width 2}))))
      (is (= :W2 (choose-by-width (loc-with-node {:width 3}))))
      (is (= :WB (choose-by-width (loc-with-node {:width 4}))))
      (is (= :WB (choose-by-width (loc-with-node {:width 7}))))
      (is (= :WB (choose-by-width (loc-with-node {:width 9}))))
      (is (= :P  (choose-by-width (loc-with-node {:width -1}))))))

  (testing "predicate must resolve to fn for by-width"
    #_{:clj-kondo/ignore [:inline-def]}
    (sut/by-width bw-bad
      :not-a-fn   (fn [_] :X)
      :else       (fn [_] :Y))
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Predicate spec did not resolve to a function"
         (bw-bad (loc-with-node {:width 3}))))))