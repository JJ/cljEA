(ns
  pea.par-ea
  (:gen-class))


(require '(ea [problem :as any-problem]
              )
         )

(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              )
         )

(import '(pea.ds EvaluatorsPool))
(import '(pea.ds ReproducersPool))

(defn mk-future [& {:keys [inds2eval feeder begin-action end-action condition p-result-obtained]}]
  (let [
        current (future (begin-action inds2eval))
        then (fn []
               (let [result @current]
                 (end-action result)
                 (if (condition)
                   (mk-future :inds2eval (feeder) :feeder feeder :begin-action begin-action
                              :end-action end-action :condition condition :p-result-obtained p-result-obtained)
                   (deliver p-result-obtained true)
                   )
                 )
               )
        ]
    (future (then))
    )
  )


(def par-problem
  {
   :runParCEvals (fn [self r-obtained-notification]
                   (let [
                         config (assoc (.config self) :ff (any-problem/fitnessFunction self) :qf (fn [_] false) :df (fn [_]))
                         result (promise)
                         best-solution (atom [nil -1])
                         evaluations (atom 0)
                         p-r-obtained (promise)
                         p2-rep (ReproducersPool. (comparator (fn [x y] (> (nth x 1) (nth y 1)))))
                         p2-eval (EvaluatorsPool. (any-problem/getPop self))
                         ]

                     (future (#(let[_ @p-r-obtained] (r-obtained-notification @best-solution @evaluations))))

                     (doseq [_ (range (:EvaluatorsCount config))]
                       (let[inds2Eval1 (.extractElements p2-eval (:EvaluatorsCapacity config))]
                         (mk-future :inds2eval inds2Eval1 :feeder #(.extractElements p2-eval (:EvaluatorsCapacity config))
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
                                                        (swap! best-solution #(identity %2) best)
                                                        )
                                                      )
                                                    )
                                                  )
                                    :condition #(< @evaluations (:Evaluations config))

                                    :p-result-obtained p-r-obtained)
                         )
                       )

                     (doseq [_ (range (:ReproducersCount config))]
                       (let[inds2Eval1 (.extractElements p2-rep (:ReproducersCapacity config))]
                         (mk-future :inds2eval inds2Eval1 :feeder #(.extractElements p2-rep (:ReproducersCapacity config))
                                    :begin-action (fn[i-evals]
                                                    (reproducer/reproduce :config config :iEvals i-evals)
                                                    )
                                    :end-action (fn[rResult]
                                                  (when (> (count rResult) 0)
                                                    (.append p2-eval rResult)
                                                    )
                                                  )

                                    :condition #(< @evaluations (:Evaluations config))

                                    :p-result-obtained p-r-obtained)
                         )
                       )
                     :ok
                     )
                   )
   })
