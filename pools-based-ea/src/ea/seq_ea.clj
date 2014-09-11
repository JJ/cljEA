(ns
  ea.seq-ea
  (:gen-class))

(require '(ea [problem :as problem-alias]
              )
         )


(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              )
         )

(def seq-problem
  {
   :runSeqCEvals (fn [self]
                   (let [ best-solution (atom [nil -1]) config (assoc (.config self) :ff (problem-alias/fitnessFunction self) :qf (fn [_] false) :df (fn [_]))]
                     (loop [p2Eval (problem-alias/getPop self)]
                       (let [
                             indEvals (evaluator/evaluate :config config :p2Eval p2Eval)
                             ordIndEvals (sort-by #(nth % 1) > indEvals)
                             best (first ordIndEvals)
                             ]
                         (when (< (nth @best-solution 1) (nth best 1))
                           (reset! best-solution best)
                           )
                         (if (< (swap! (.Evaluations self) #(+ % (count indEvals))) (:Evaluations (.config self)))
                           (recur (reproducer/reproduce :config config :iEvals ordIndEvals))
                           [@best-solution @(.Evaluations self)]
                           )
                         )
                       )
                     )
                   )

   :runSeqFitnessQuality (fn [self]
                           (let [
                                 alcanzada-solucion (atom false)
                                 best-solution (atom [nil -1])
                                 config (assoc (.config self) :ff (problem-alias/fitnessFunction self) :qf (problem-alias/qualityFitnessFunction self)
                                          :df (fn [indEval]
                                                (reset! alcanzada-solucion true)
                                                (reset! best-solution indEval)
                                                )
                                          )
                                 ]
                             (loop [p2Eval (problem-alias/getPop self)]
                               (let [
                                     indEvals (evaluator/evaluate :config config :p2Eval p2Eval)
                                     ordIndEvals (sort-by #(nth % 1) > indEvals)
                                     ]
                                 (swap! (.Evaluations self) #(+ % (count indEvals)))
                                 (if-not @alcanzada-solucion
                                   (recur (reproducer/reproduce :config config :iEvals ordIndEvals))
                                   [@best-solution @(.Evaluations self)]
                                   )
                                 )
                               )
                             )
                           )
   })
