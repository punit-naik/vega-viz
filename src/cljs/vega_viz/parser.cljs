(ns vega-viz.parser
  (:require cljsjs.vega cljsjs.vega-lite cljsjs.vega-embed cljsjs.vega-tooltip
            [reagent.core :as r]
            [promesa.core :as p]))

(defn- parse-vl-spec [spec elem]
  (when spec
    (let [opts {:renderer "canvas"
                :mode "vega-lite"}]
      (js/vegaEmbed elem (clj->js spec) (clj->js opts)))))

(defn- vega-lite
  "Reagent component that renders vega-lite."
  [spec]
  (r/create-class
   {:display-name "vega-lite"
    :component-did-mount (fn [this]
                           (parse-vl-spec spec (r/dom-node this)))
    :component-will-update (fn [this [_ new-spec]]
                             (parse-vl-spec new-spec (r/dom-node this)))
    :reagent-render (fn [spec]
                      [:div#vega-viz])}))

(defn plot
  "Renders the Hiccup style plot from a spec
   To be used only inside Hiccup style HTML data structure to be rendered"
  [spec]
  [vega-lite spec])
