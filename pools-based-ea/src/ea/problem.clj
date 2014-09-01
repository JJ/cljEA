(ns
  ea.problem
  (:gen-class))


(defprotocol Problem

  (fitnessFunction [self])
  (qualityFitnessFunction [self v])

  (genIndividual [self])
  (getPop [self])

  (runSeqCEvals [self])
  (runParCEvals [self r-obtained-notification])

  )

(require '(ea [evaluator :as evaluator]
              [reproducer :as reproducer]
              [seq-ea :as seq-ea]
              )
         )

(require '(pea [par-ea :as par-ea]
               )
         )

(def any-problem
  { :genIndividual (fn [self]
                     (for [_ (range (:ChromosomeSize (.config self)))] (rand-int 2))
                     )

    :getPop (fn [self]
              (for [_ (range (:PopSize (.config self)))] (genIndividual self))
              )

    })


(defrecord MaxOne[config Emigrations Evaluations])

(defn create-maxOneProblem [conf]
  (MaxOne. conf (atom 0) (atom 0))
  )

(extend MaxOne
  Problem  (merge any-problem seq-ea/seq-problem par-ea/par-problem {
                                                                     :fitnessFunction (fn[self]
                                                                                        #(count (for [a % :when (= a 1)] a))
                                                                                        )

                                                                     :qualityFitnessFunction (fn[self]
                                                                                               #(> % (- (:ChromosomeSize (.config self)) 2))
                                                                                               )
                                                                     }

                  )
  )
