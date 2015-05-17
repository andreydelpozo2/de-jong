(ns de-jong.transition
  (:require [de-jong.points-calculator :refer [in-range]]))

(def tau (* 2 js/Math.PI))

(defn sign [x]
  (cond
    (> x 0) 1
    (< x 0) -1
    (= x 0) 0))

(defn displacement-vector
  "Finds the displacement vector between two position vectors in a torus
  generated by the product of n unit-circles"
  [initial final]
  {:pre [(every? in-range (concat initial final))]}
  (let [f (fn [q-i p-i]
            (let [in-plane    (- q-i p-i)
                  wrap-around (* (- tau (js/Math.abs in-plane)) (- (sign in-plane)))]
              (if (<= (js/Math.abs in-plane) (js/Math.abs wrap-around))
                in-plane
                wrap-around)))]
    (vec (map f final initial))))

(defn lerp [t initial final]
  {:pre [(and (>= t 0) (<= t 1))]}
  (+ initial (* t (- final initial))))

(defn lerp-vectors [initial final num-frames]
  (let [step     (/ 1 num-frames)
        vec-lerp (fn [t i f] (map (partial lerp t) i f))]
    (map #(vec-lerp % initial final) (range 0 1 step))))

(defn lerp-vectors-torus [initial final num-frames]
  (let [displacement  (displacement-vector initial final)
        final         (map + initial displacement)]
    (lerp-vectors initial final num-frames)))
