(ns
  pea.par-ea
  (:gen-class))


(require '(ea [problem :as problem-alias]
              )
         )

(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              )
         )

(import '(pea.ds EvaluatorsPool))
(import '(pea.ds ReproducersPool))

(defn mk-worker [& {:keys [inds2eval feeder begin-action end-action condition p-result-obtained]}]
  (loop [inds-2-eval inds2eval]
    (let [
          current (future (begin-action inds-2-eval))
          result @current
          ]
      (end-action result)
      (when (condition)
        (recur (feeder))
        )
      )
    )
  (deliver p-result-obtained true)
  )


(def par-problem
  {
   :runParCEvals (fn [self r-obtained-notification]
                   (let [
                         config (assoc (.config self) :ff (problem-alias/fitnessFunction self) :qf (fn [_] false) :df (fn [_]))
                         result (promise)
                         best-solution (atom [nil -1])
                         evaluations (atom 0)
                         p-r-obtained (promise)
                         p2-rep (ReproducersPool. (comparator (fn [x y] (> (nth x 1) (nth y 1)))))
                         p2-eval (EvaluatorsPool. (problem-alias/getPop self))
                         ]

                     (future (#(let[_ @p-r-obtained] (r-obtained-notification @best-solution @evaluations))))

                     (doseq [_ (range (:EvaluatorsCount config))]
                       (let[inds2Eval1 (.extractElements p2-eval (:EvaluatorsCapacity config))]
                         (future (mk-worker :inds2eval inds2Eval1 :feeder #(.extractElements p2-eval (:EvaluatorsCapacity config))
                                            :begin-action (fn[inds2Eval]
                                                            (let[evals (evaluator/evaluate :config config :p2Eval inds2Eval)]
                                                              (sort-by #(nth % 1) > evals)
                                                              )
                                                            )
                                            :end-action (fn[eResult]
                                                          (when (> (count eResult) 0)
                                                            (let [best (first eResult)]
                                                              (.append p2-rep eResult)
                                                              (swap! evaluations #(identity (+ %1 %2)) (count eResult))
                                                              (when (< (nth @best-solution 1) (nth best 1))
                                                                (reset! best-solution best)
                                                                )
                                                              )
                                                            )
                                                          )
                                            :condition #(< @evaluations (:Evaluations config))

                                            :p-result-obtained p-r-obtained))
                         )
                       )

                     (doseq [_ (range (:ReproducersCount config))]
                       (let[inds2Eval1 (.extractElements p2-rep (:ReproducersCapacity config))]
                         (future (mk-worker :inds2eval inds2Eval1 :feeder #(.extractElements p2-rep (:ReproducersCapacity config))
                                            :begin-action (fn[i-evals]
                                                            (reproducer/reproduce :config config :iEvals i-evals)
                                                            )
                                            :end-action (fn[rResult]
                                                          (when (> (count rResult) 0)
                                                            (.append p2-eval rResult)
                                                            )
                                                          )

                                            :condition #(< @evaluations (:Evaluations config))

                                            :p-result-obtained p-r-obtained))
                         )
                       )
                     :ok
                     )
                   )

   :runParFitnessQuality (fn [self r-obtained-notification]
                           (let [
                                 alcanzada-solucion (atom false)
                                 best-solution (atom [nil -1])
                                 config (assoc (.config self) :ff (problem-alias/fitnessFunction self) :qf (problem-alias/qualityFitnessFunction self)
                                          :df (fn [indEval]
                                                (reset! alcanzada-solucion true)
                                                (reset! best-solution indEval)
                                                ))
                                 result (promise)
                                 evaluations (atom 0)
                                 p-r-obtained (promise)
                                 p2-rep (ReproducersPool. (comparator (fn [x y] (> (nth x 1) (nth y 1)))))
                                 p2-eval (EvaluatorsPool. (problem-alias/getPop self))
                                 ]

                             (future (#(let[_ @p-r-obtained] (r-obtained-notification @best-solution @evaluations))))

                             (doseq [_ (range (:EvaluatorsCount config))]
                               (let[inds2Eval1 (.extractElements p2-eval (:EvaluatorsCapacity config))]
                                 (future (mk-worker :inds2eval inds2Eval1 :feeder #(.extractElements p2-eval (:EvaluatorsCapacity config))
                                                    :begin-action (fn[inds2Eval]
                                                                    (let[evals (evaluator/evaluate :config config :p2Eval inds2Eval)]
                                                                      (sort-by #(nth % 1) > evals)
                                                                      )
                                                                    )
                                                    :end-action (fn[eResult]
                                                                  (when (> (count eResult) 0)
                                                                    (let [best (first eResult)]
                                                                      (.append p2-rep eResult)
                                                                      (swap! evaluations #(identity (+ %1 %2)) (count eResult))
                                                                      )
                                                                    )
                                                                  )
                                                    :condition #(= @alcanzada-solucion false)

                                                    :p-result-obtained p-r-obtained))
                                 )
                               )

                             (doseq [_ (range (:ReproducersCount config))]
                               (let[inds2Eval1 (.extractElements p2-rep (:ReproducersCapacity config))]
                                 (future (mk-worker :inds2eval inds2Eval1 :feeder #(.extractElements p2-rep (:ReproducersCapacity config))
                                                    :begin-action (fn[i-evals]
                                                                    (reproducer/reproduce :config config :iEvals i-evals)
                                                                    )
                                                    :end-action (fn[rResult]
                                                                  (when (> (count rResult) 0)
                                                                    (.append p2-eval rResult)
                                                                    )
                                                                  )

                                                    :condition #(= @alcanzada-solucion false)

                                                    :p-result-obtained p-r-obtained))
                                 )
                               )
                             :ok
                             )
                           )
   })
