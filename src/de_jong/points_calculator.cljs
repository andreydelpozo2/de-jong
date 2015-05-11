(ns de-jong.points-calculator
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om.dom :as dom]
            [cljs.core.async :refer [>! take! chan]]
            [de-jong.ifs-viewer :refer [ifs-viewer]]))

(def points-to-draw (js/Math.pow 2 13))

(defn de-jong-ifs [a b c d]
  {:pre [(every? #(and (<= % js/Math.PI) (>= % (- js/Math.PI))) [a b c d])]}
  (fn [[x y z]]
    [(- (js/Math.sin (* a y)) (js/Math.cos (* b x)))
     (- (js/Math.sin (* c x)) (js/Math.cos (* d y)))
     (- (js/Math.sin (* 2.0 x)) (js/Math.cos (* 2.0 y)))]))

(defn random-points [minimum maximum]
  (let [random-val #(+ (rand (- maximum minimum)) minimum)]
    (map vec (partition 3 (repeatedly random-val)))))

(defn point-emitter [f]
  (let [comm (chan)
        points (atom (take points-to-draw (random-points -2.0 2.0)))]
    (go (while true
          (>! comm @points)
          (swap! points (partial map f))))
    comm))

(defn set-channel! [owner ifs-params]
  (let [ifs  (apply de-jong-ifs ifs-params)
        comm (point-emitter ifs)]
    (om/set-state! owner :comm comm)))

(defn points-calculator [ifs-params owner]
  (reify
    om/IInitState
    (init-state [_]
      { :points nil })
    om/IDidMount
    (did-mount [_]
      (set-channel! owner ifs-params))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [comm (om/get-state owner :comm)]
        (take! comm (partial om/set-state! owner :points))))
    om/IWillReceiveProps
    (will-receive-props [_ _]
      (set-channel! owner ifs-params))
    om/IRenderState
    (render-state [_ state]
      (om/build ifs-viewer (:points state)))))
