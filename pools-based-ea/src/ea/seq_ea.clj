(ns
  ea.seq-ea
  (:gen-class))

(require '(ea [problem :as any-problem]
              )
         )


(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              )
         )

(def seq-problem
  {
   :runSeqCEvals (fn [self]
                   (let [config (assoc (.config self) :ff (any-problem/fitnessFunction self) :qf (fn [_] false) :df (fn [_]))]
                     (loop [p2Eval (any-problem/getPop self)]
                       (let [indEvals (evaluator/evaluate :config config :p2Eval p2Eval)
                             ordIndEvals (sort-by #(nth % 1) > indEvals)]
                         (if (< (swap! (.Evaluations self) #(+ % (count indEvals))) (:Evaluations (.config self)))
                           (recur (reproducer/reproduce :config config :iEvals ordIndEvals))
                           [(first ordIndEvals) @(.Evaluations self)]
                           )
                         )
                       )
                     )
                   )
   })