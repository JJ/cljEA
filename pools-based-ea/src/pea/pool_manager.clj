(ns
  pea.pool-manager
  (:gen-class))

(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              )
         )

(import '(pea.ds EvaluatorsPool))
(import '(pea.ds ReproducersPool))


(defprotocol PoolManager
  (start [self r-obtained-notification])
  )


(defrecord PoolManagerCEvals[problem cEvaluations])

(defn create-PoolManagerCEvals[problem cEvaluations]
  (PoolManagerCEvals. problem cEvaluations)
  )

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


(extend-type PoolManagerCEvals

  PoolManager
  (start [self r-obtained-notification]
         (let [
               result (promise)
               best-solution (atom [nil -1])
               evaluations (atom 0)
               emigrations (atom 0)
               p-r-obtained (promise)

               p2-rep (ReproducersPool. (comparator (fn [x y] (< (nth x 1) (nth y 1)))))
               getPop (:getPop (:problem self))
               p2-eval (EvaluatorsPool. (getPop))
               ]

           (future (#(let[_ @p-r-obtained] (r-obtained-notification @best-solution @evaluations @emigrations))))

           (doseq [_ (range (:EvaluatorsCount (:problem self)))]
             (let[inds2Eval1 (.extractElements p2-eval (:EvaluatorsCapacity (:problem self)))]
               (mk-future :inds2eval inds2Eval1 :feeder #(.extractElements p2-eval (:EvaluatorsCapacity (:problem self)))
                          :begin-action (fn[inds2Eval]
                                          (let[evals (evaluator/evaluate :config (:problem self) :p2Eval inds2Eval)]
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
                          :condition #(< @evaluations (:cEvaluations self))

                          :p-result-obtained p-r-obtained)


               )
             )

           (doseq [_ (range (:ReproducersCount (:problem self)))]
             (let[inds2Eval1 (.extractElements p2-rep (:ReproducersCapacity (:problem self)))]
               (mk-future :inds2eval inds2Eval1 :feeder #(.extractElements p2-rep (:ReproducersCapacity (:problem self)))
                          :begin-action (fn[i-evals]
                                          (reproducer/reproduce :config (:problem self) :iEvals i-evals)
                                          )
                          :end-action (fn[rResult]
                                        (when (> (count rResult) 0)
                                          (.append p2-eval rResult)
                                          ; TODO: Migrations...
                                          )
                                        )

                          :condition #(< @evaluations (:cEvaluations self))

                          :p-result-obtained p-r-obtained)

               )
             )

           :ok
           )
         )
  )
