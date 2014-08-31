(ns
  ea.problem
  (:gen-class))

(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              )
         )

(require '(pea [pool-manager :as pool-manager]
               )
         )

(defprotocol Problem

  (fitnessFunction [self])
  (qualityFitnessFunction [self v])

  (genIndividual [self])
  (getPop [self])

  (runSeqCEvals [self])
  (runParCEvals [self])

  )

(def any-problem
  { :genIndividual (fn [self]
                     (for [_ (range (:ChromosomeSize (.config self)))] (rand-int 2))
                     )

    :getPop (fn [self]
              (for [_ (range (:PopSize (.config self)))] (genIndividual self))
              )

    :runSeqCEvals (fn [self]
                    (let [config (assoc (.config self) :ff (fitnessFunction self) :qf (fn [_] false) :df (fn [_]))]
                      (loop [p2Eval (getPop self)]
                        (let [indEvals (evaluator/evaluate :config config :p2Eval p2Eval)
                              ordIndEvals (sort-by #(nth % 1) > indEvals)]
                          (if (< (swap! (.Evaluations self) #(+ % (count indEvals))) (:Evaluations (.config self)))
                            (recur (reproducer/reproduce :config config :iEvals ordIndEvals))
                            (first ordIndEvals)
                            )
                          )
                        )
                      )
                    )

    :runParCEvals (fn [self]
                    (let [
                          p-manager (pool-manager/create-PoolManagerCEvals (assoc (.config self)
                                                                             :getPop #(getPop self)
                                                                             :ff (fitnessFunction self)
                                                                             :qf (fn [_] false) :df (fn [_])) 5000)
                          n-result (fn[sol evals emigrs]
                                     (println (nth sol 1))
                                     )
                          ]
                      (pool-manager/start p-manager n-result)
                      )
                    )

    })


(defrecord MaxOne[config Emigrations Evaluations])

(defn create-maxOneProblem [conf]
  (MaxOne. conf (atom 0) (atom 0))
  )

(extend MaxOne
  Problem  (assoc any-problem
             :fitnessFunction (fn[self]
                                (let [fit (fn [ind]
                                            ;(println "Calculando")
                                            (let [res (count (for [a ind :when (= a 1)] a))]
                                             ; (println "El resultado fue: " res)
                                              res
                                              )
                                            )]
                                  fit
                                  )
                                ;#(count (for [a % :when (= a 1)] a))
                                )

             :qualityFitnessFunction (fn[self]
                                       #(> % (- (:ChromosomeSize (.config self)) 2))
                                       )

             )
  )
