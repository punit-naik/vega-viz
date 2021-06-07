(ns vega-viz.data)


(defn add-dataset
  "Adds a dataset to the Vega-lite spec"
  [vl-spec data]
  (assoc-in vl-spec [:data :values] data))

