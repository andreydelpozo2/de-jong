(ns de-jong.components.app
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [de-jong.components.point-cloud :refer [full-screen-point-cloud]]
            [de-jong.components.editor :refer [editor]]
            [de-jong.components.params-picker :refer [params-picker]]
            [de-jong.transition :refer [transition-params]]))

(defn advance-frame! [owner]
  (let [{ :keys [frames frame-number] } (om/get-state owner)]
    (om/set-state! owner :frame-number (mod (inc frame-number) (count frames)))))

(def make-transition (comp vec (partial transition-params 120)))

(defn app [{:keys [selection ifs-params] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
      { :frames (make-transition ifs-params)
        :frame-number 0 })
    om/IWillReceiveProps
    (will-receive-props [this {:keys [ifs-params] :as next-props}]
      (let [old-ifs-params (om/get-props owner :ifs-params)]
        (if-not (= old-ifs-params ifs-params)
          (om/set-state! owner :frames (make-transition ifs-params)))))
    om/IDidMount
    (did-mount [_]
      (advance-frame! owner))
    om/IDidUpdate
     (did-update [_ _ _]
       (advance-frame! owner))
    om/IRenderState
    (render-state [this {:keys [frames frame-number]}]
      (dom/div nil
        (om/build editor data)
        (dom/div #js {:className "params-picker-container"}
          (om/build params-picker (nth ifs-params (:idx selection))))
        (om/build full-screen-point-cloud [(nth frames frame-number) (js/Math.pow 2 18)])))))