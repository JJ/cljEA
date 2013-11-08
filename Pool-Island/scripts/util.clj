(ns pea)

(defn get-status [table]
  (let [
         evals (for [[_ [fitness state]] table
                     :when (= state 2)]
                 fitness
                 )
         noEvals (for [[ind [_ state]] table
                       :when (= state 1)]
                   ind
                   )
         ]

    {
      :evals (count evals)
      :noEvals (count noEvals)
      :max (reduce #(if (< %1 %2) %2 %1) evals)
      ;      :min (reduce #(if (> %1 %2) %2 %1) evals)
      }
    )
  )

