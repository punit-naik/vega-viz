(ns vega-viz.core)

(defonce ^:private initial-spec
  {:width  400
   :height 200
   :padding {:top 10 :left 30 :bottom 30 :right 10}})

(defn init!
  "Initialises the Vega-lite spec with default values and returns the same"
  []
  initial-spec)
