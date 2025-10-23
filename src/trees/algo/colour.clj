(ns trees.algo.colour
  (:require [clojure.string :as str]))

(defn clamp8 [n] (-> n (max 0) (min 255) int))

(defn hex->rgba
  "Accepts #RGB, #RGBA, #RRGGBB, #RRGGBBAA, or 0xAARRGGBB (int).
   Returns [r g b a] with 0–255 ints."
  [h]
  (cond
    (integer? h) ; assume 0xAARRGGBB (Processing/Java style ARGB int)
    (let [a (bit-and 0xFF (unsigned-bit-shift-right h 24))
          r (bit-and 0xFF (unsigned-bit-shift-right h 16))
          g (bit-and 0xFF (unsigned-bit-shift-right h 8))
          b (bit-and 0xFF h)]
      [r g b a])

    (string? h)
    (let [s (-> h str/trim (str/replace #"^#" ""))]
      (case (count s)
        3 (let [[r g b] (map #(Integer/parseInt (str % %) 16) s)]
            [r g b 255])
        4 (let [[r g b a] (map #(Integer/parseInt (str % %) 16) s)]
            [r g b a])
        6 (let [r (Integer/parseInt (subs s 0 2) 16)
                g (Integer/parseInt (subs s 2 4) 16)
                b (Integer/parseInt (subs s 4 6) 16)]
            [r g b 255])
        8 (let [r (Integer/parseInt (subs s 0 2) 16)
                g (Integer/parseInt (subs s 2 4) 16)
                b (Integer/parseInt (subs s 4 6) 16)
                a (Integer/parseInt (subs s 6 8) 16)]
            [r g b a])
        (throw (ex-info "Bad hex length" {:value h}))))
    :else (throw (ex-info "Unsupported colour literal" {:value h}))))

(defn rgba->hex8 [[r g b a]]
  (format "#%02X%02X%02X%02X" (clamp8 r) (clamp8 g) (clamp8 b) (clamp8 a)))

(defn ensure-rgba
  "Normalises various inputs to [r g b a] 0–255."
  [c]
  (cond
    (and (vector? c) (= 3 (count c))) (conj (mapv clamp8 c) 255)
    (and (vector? c) (= 4 (count c))) (mapv clamp8 c)
    :else (hex->rgba c)))

;; ------------------------------------------------------------
;; Colour interpolation algorithms
;; ------------------------------------------------------------

(defn- clamp01 [x]
  (-> (double x) (max 0.0) (min 1.0)))

(defn- mix-channel [a b t]
  (let [v (+ a (* t (- b a)))]
    (clamp8 (Math/round (double v)))))

(defn- mix-rgba [[r0 g0 b0 a0] [r1 g1 b1 a1] t]
  [(mix-channel r0 r1 t)
   (mix-channel g0 g1 t)
   (mix-channel b0 b1 t)
   (mix-channel a0 a1 t)])

(defn- norm01
  "Map v in the domain [lo, hi] to t in [0, 1], clamped. Works for lo>hi as well."
  [v lo hi]
  (let [lo* (double lo)
        hi* (double hi)
        v*  (double v)
        denom (- hi* lo*)]
    (if (zero? denom)
      0.0
      (clamp01 (/ (- v* lo*) denom)))))

(defn between
  "Generic colour interpolation builder.

   Returns a function of [loc] -> [r g b a] that linearly interpolates between
   START and FINISH colours according to an accessor-produced scalar mapped from
   [lo,hi] → [0,1], then transformed by EASE.

   - start, finish: any accepted colour literal (hex, int ARGB, [r g b], [r g b a])
   - accessor: fn of loc -> number (the basis)
   - lo, hi: numeric domain anchors for accessor values (order can be reversed)
   - ease: fn t->t' in [0,1] (e.g., identity, pow, smoothstep)"
  [start finish accessor lo hi ease]
  (let [c0 (ensure-rgba start)
        c1 (ensure-rgba finish)
        ease* (or ease identity)]
    (fn [loc]
      (let [v (accessor loc)
            t (norm01 (or v 0.0) lo hi)
            t' (double (ease* t))]
        (mix-rgba c0 c1 t')))))

(defn linear
  "Linear colour interpolation between START and FINISH.

   accessor: fn loc -> number; lo, hi: numeric anchors for mapping to [0,1]."
  [start finish accessor lo hi]
  (between start finish accessor lo hi identity))

(defn gamma
  "Gamma-eased colour interpolation.

   exponent > 1: slow near base (t≈0), faster near tips (t→1).
   exponent < 1: fast near base, slower near tips."
  [start finish accessor lo hi exponent]
  (let [ease (fn [t] (Math/pow (double t) (double exponent)))]
    (between start finish accessor lo hi ease)))

(defn smoothstep
  "Smoothstep easing colour interpolation (slow at both ends, faster in middle)."
  [start finish accessor lo hi]
  (let [ease (fn [t]
               (let [t (double t)]
                 (* t t (- 3.0 (* 2.0 t)))))]
    (between start finish accessor lo hi ease)))

