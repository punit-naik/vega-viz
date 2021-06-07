(ns vega-viz.core
  (:require [vega-viz.data :refer [add-dataset]]
            [vega-viz.encoding :refer [add-axes]]
            [vega-viz.mark :refer [add-mark]]
            [vega-viz.transform :refer [stack apply-filter add-rule-for-line]]
            [vega-viz.animations :refer [select-on-click]]))

(defonce ^:private initial-spec
  {:width  "container"
   :height "container"
   :background "transparent"
   :config {:view {:stroke "transparent"}
            :mark {:cursor "pointer"}}})

(defn init!
  "Initialises the Vega-lite spec with default values and returns the same"
  []
  initial-spec)

(defn increase-hover-area
  "Adds a selection in the `spec` to increase the hover area of a point on the chart
   So that tooltips show even if the cursor is not exactly on the point"
  [spec]
  (-> spec
      (update :selection assoc :hover {:type "single" :on "mouseover" :empty "none" :nearest true :clear "mouseout"})))

(defn get-tick-count
  "Returns the tick count of axes based on `data`"
  [data]
  (cond
    (= (count data) 1) 2
    (>= (count data) 10) 10
    :else (count data)))

(defn gen-chart-spec
  [id
   data
   {:keys [x-fld-name x-fld-type x-fld-title]
    :or {x-fld-name "x"
         x-fld-type "temporal"
         x-fld-title "Time"}}
   {:keys [y-fld-name y-fld-type y-fld-title]
    :or {y-fld-name "y"
         y-fld-type "quantitative"
         y-fld-title "Count"}}
   {:keys [chart-type chart-interpolate tooltip? point?
           bucket bucket-mapper]
    :or {chart-type "line"
         chart-interpolate "monotone"
         tooltip? true
         point? false
         bucket "1d"
         bucket-mapper {"1d" "datemonthyear"
                        "1w" "datemonthyear"
                        "1M" "monthyear"
                        "1q" "datemonthyear"
                        "1y" "year"}}}
   {:keys [stack-fld-name stack-fld-type stack-fld-title stack-fld-opts]
    :or {stack-fld-name "label"
         stack-fld-type "nominal"
         stack-fld-title "Labels"}
    :as stack-info}]
  (let [temporal-axis-tick-values (->> (map (if (= x-fld-type "temporal")
                                              :x :y) data)
                                       distinct sort)
        ;; The reason the user won't see extra ticks is because of the value
        ;; This will set the max ticks to be 10
        ;; Will make tick-count to `2` if the data count is 1,
        ;; to display the chart values
        tick-count (get-tick-count data)
        bucket (or bucket "1d")
        time-unit (if (= chart-type "moving-avg")
                    "datemonthyear"
                    (get bucket-mapper bucket))]
    (cond-> (-> (init!)
                (add-dataset data)
                (assoc :id id)
                (add-axes {:x-fld-name x-fld-name :x-fld-type x-fld-type
                           :x-fld-opts (cond-> {:title x-fld-title
                                                :axis (merge {:labelAngle 0 :labelOverlap false :grid false
                                                              :titleFontSize 14
                                                              :labelFontSize 12
                                                              :tickCount tick-count}
                                                             (when (and (not= x-fld-type "temporal")
                                                                        (= chart-type "bar"))
                                                               {:tickOffset {:expr (str "width/" (* -2.25 tick-count))}}))}
                                         (= x-fld-type "temporal") (assoc :timeUnit time-unit)
                                         (= x-fld-type "temporal") (assoc-in [:axis :labelAngle] -25)
                                         (= x-fld-type "temporal") (assoc-in [:axis :values]
                                                                             temporal-axis-tick-values)
                                         (= y-fld-type "temporal") (assoc :impute {:value 0}))
                           :y-fld-name y-fld-name :y-fld-type y-fld-type
                           :y-fld-opts (cond-> {:title y-fld-title
                                                :axis {:labelAngle 0 :labelOverlap false :grid false
                                                       :titleFontSize 14
                                                       :labelFontSize 12
                                                       :tickCount tick-count}}
                                         (= y-fld-type "temporal") (assoc :timeUnit time-unit)
                                         (= y-fld-type "temporal") (assoc-in [:axis :labelAngle] -25)
                                         (= y-fld-type "temporal") (assoc-in [:axis :values]
                                                                             temporal-axis-tick-values)
                                         (= x-fld-type "temporal") (assoc :impute {:value 0}))})
                (add-mark (cond-> {:type chart-type
                                   :invalid "filter"}
                            (= chart-type "moving-avg") (assoc :type "line")
                            (or (= chart-type "line")
                                (= chart-type "moving-avg")) (assoc :interpolate chart-interpolate)
                            (and (or (= chart-type "line")
                                     (= chart-type "moving-avg"))
                                 point?) (assoc :point {:filled true :size 60})))
                (apply-filter {:field "x" :valid true})
                (apply-filter {:field "y" :valid true}))
      (and (> (count data) 1)
           (contains? #{"line" "moving-avg"} chart-type)) (add-rule-for-line chart-type stack-fld-name chart-interpolate)
      stack-info (stack {:stack-fld stack-fld-name :stack-fld-type stack-fld-type
                         :stack-fld-opts (merge {:title stack-fld-title :legend {:orient "top"}}
                                                stack-fld-opts)})
      stack-info (select-on-click {:select-fld stack-fld-name :select-name :company})
      tooltip? (update :encoding (fn [enc] (assoc enc :tooltip [{:field x-fld-name :title x-fld-title :type x-fld-type}
                                                                {:field y-fld-name :title y-fld-title :type y-fld-type}]))))))
