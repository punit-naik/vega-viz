(ns vega-viz.component-view)

(defn set-height
  "Sets the height of the Vega-lite component"
  [vl-spec value]
  (assoc vl-spec :height value))

(defn set-width
  "Sets the width of the Vega-lite component"
  [vl-spec value]
  (assoc vl-spec :width value))

(defn set-padding
  "Sets the padding of the Vega-lite component
   `value` is a map of keys [:top :bottom :left :right]"
  [vl-spec value]
  (assoc vl-spec :padding value))

(defn add-title
  "Adds a title to the Vega-lite spec"
  [vl-spec title]
  (assoc vl-spec :title title))

(defn add-name
  "Adds a name to the Vega-lite spec"
  [vl-spec vl-name]
  (assoc vl-spec :title vl-name))

(defn add-desc
  "Adds a description to the Vega-lite spec"
  [vl-spec desc]
  (assoc vl-spec :description desc))
