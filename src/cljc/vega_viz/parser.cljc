(ns vega-viz.parser
  #?(:cljs (:require cljsjs.vega cljsjs.vega-lite cljsjs.vega-embed cljsjs.vega-tooltip
                     [reagent.core :as r])))

(defn- parse-vl-spec
  [spec elem click-handler-fn]
  #?(:cljs
     (when (seq spec)
       (let [click-listener-name "data-point-click"
             opts {:renderer "canvas"
                   :mode "vega-lite"
                   :actions false
                   :tooltip {:theme "dark"}
                   ;; For click handler, we have to `patch`
                   ;; As signals are not officially supported in `vega-lite`
                   :patch (fn [spec]
                            (.push
                             (.-signals spec)
                             #js {"name" click-listener-name
                                  "value" 0
                                  "on" #js [#js {"events" "mousedown"
                                                 "update" "datum"}]})
                            spec)}]
         (-> (js/vegaEmbed elem (clj->js spec) (clj->js opts))
             ;; Adding signal listener for clicks
             (.then (fn [result]
                      (.addSignalListener (get (js->clj result) "view")
                                          click-listener-name
                                          click-handler-fn)
                      result)))))))

(defn vega-lite
  "Reagent component that renders vega-lite."
  [spec click-handler-fn]
  #?(:cljs (r/create-class
            {:display-name "vega-lite"
             :component-did-mount (fn [this]
                                    (js/console.log (str "Vega: " (:id spec) " Mount"))
                                    (parse-vl-spec spec (r/dom-node this) click-handler-fn))
             :component-will-update (fn [this [_ new-spec]]
                                      (js/console.log (str "Vega: " (:id spec) " Update"))
                                      (parse-vl-spec new-spec (r/dom-node this) click-handler-fn))
             :reagent-render (fn [_]
                               [:div {:id (:id spec)
                                      :style {:width "100%" :height "100%"}}])})))

(defn default-click [_ data-point]
  #?(:cljs (js/console.log "You clicked " data-point)))

(defn plot
  "Renders the Hiccup style plot from a spec
   To be used only inside Hiccup style HTML data structure to be rendered"
  ([spec] (plot spec default-click))
  ([spec on-click-handler-fn]
   (vega-lite spec on-click-handler-fn)))
