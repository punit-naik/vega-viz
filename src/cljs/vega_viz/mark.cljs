(ns vega-viz.mark)

(defn add-mark
  "Adds a mark (plot) type to the Vega-lite spec"
  [vl-spec mtype]
  (assoc vl-spec :mark mtype))
