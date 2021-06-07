(ns vega-viz.encoding)

(defn add-axes
  "Adds axes to the plot in the Vega-lite spec"
  [{:keys [encoding] :as vl-spec}
   {:keys [x-fld-name x-fld-type x-agg-op x-fld-opts
           y-fld-name y-fld-type y-agg-op y-fld-opts]
    :or {x-fld-opts {} y-fld-opts {}}}]
  (let [x-axis (conj {:field x-fld-name
                      :type x-fld-type}
                     x-fld-opts)
        y-axis (conj {:field y-fld-name
                      :type y-fld-type}
                     y-fld-opts)
        x-axis-with-agg (if x-agg-op (assoc x-axis :aggregate x-agg-op) x-axis)
        y-axis-with-agg (if y-agg-op (assoc y-axis :aggregate y-agg-op) y-axis)
        axes {:x x-axis-with-agg :y y-axis-with-agg}]
    (if encoding
      (update vl-spec :encoding conj axes)
      (assoc vl-spec :encoding axes))))

(defn get-fld-info
  "Gets the info of the field on `fld`-Axis"
  [spec fld]
  (get-in spec [:encoding fld]))

(defn add-dashes
  "Adds dashes to the stroke of a line chart"
  ([spec fld-name] (add-dashes spec fld-name "nominal"))
  ([spec fld-name fld-type]
   (update spec :encoding assoc :strokeDash {:field fld-name :type fld-type
                                             ;; We cant to hinde the legend for this, hence
                                             :legend nil})))

(defn add-opacity
  "Adds opacity/transparency to the stroke of a line chart based on `fld-name`"
  ([spec fld-name] (add-opacity spec fld-name "quantitative"))
  ([spec fld-name fld-type]
   (update spec :encoding assoc :strokeOpacity {:field fld-name :type fld-type
                                                ;; We cant to hinde the legend for this, hence
                                                :legend nil})))