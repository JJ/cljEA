;;
;; Author José Albert Cruz Almaguer <jalbertcruz@gmail.com>
;; Copyright 2013 by José Albert Cruz Almaguer.
;;
;; This program is licensed to you under the terms of version 3 of the
;; GNU Affero General Public License. This program is distributed WITHOUT
;; ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
;; MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
;; AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
;;

(ns pea)

(import 'java.util.Date)
(import 'sheduling.ShedulingUtility)

(extend-type TPoolManager
  poolManager/PoolManager
  ;agent que comparte un conjunto de individuos con
  ;agentes evaluadores y reproductores

  (finalizeAllWorkers [self]
    (doseq [e @(.evals self)]
      (send e finalize/finalize)
      )
    (doseq [e @(.reps self)]
      (send e finalize/finalize)
      )
    self
    )

  (initEvaluations [self cant]
    (swap! (.evaluations self) #(identity %2) cant)
    self
    )

  (init [self conf]
    (swap! (.pmConf self) #(identity %2)
      (dissoc conf :population)
      )
    (swap! (.evals self) #(identity %2)
      (set (for [_ (range (:evaluatorsCount conf))]
             (agent (evaluator/create *agent* (.profiler self)) ;               :error-mode :continue
               :error-handler pea/evaluator-error)
             )
        )
      )

    (swap! (.reps self) #(identity %2)
      (set (for [_ (range (:reproducersCount conf))]
             (agent (reproducer/create *agent* (.profiler self)) ;               :error-mode :continue
               :error-handler pea/reproducer-error)
             )
        )
      )

    (swap! (.active self) #(identity %2) true)

    (let [
           population (:population conf)
           ]

      (swap! (.poolSize self) #(identity %2) (count population))

      (dosync
        (alter (.table self) #(identity %2)
          (into {} (for [p population] [p [-1 1]]))
          )
        )
      )
    self
    )

  (updatePool [self newPool]
    (dosync
      (alter (.table self) #(identity %2)
        newPool)
      )
    self
    )

  (add2Pool [self individuos]
    (dosync
      ;        (alter (.table self) #(identity %2)
      ; (adjustPool @(.table self) ndata @(.poolSize self)))
      (alter (.table self) #(identity %2)
        (into @(.table self) individuos))
      )
    self
    )

  (migrantsDestination [self dests]
    (swap! (.migrantsDestination self) #(identity %2) dests)
    self
    )

  (migration [self ParIndividuoFitness]
    (poolManager/add2Pool self [[(nth ParIndividuoFitness 0) [(nth ParIndividuoFitness 1) 2]]])
    self
    )

  (evaluatorFinalized [self pid]
    (when (and
            (empty? (swap! (.evals self) #(disj %1 %2) pid))
            (empty? @(.reps self))
            )
      (finalize/finalize self)
      )
    self
    )

  (reproducerFinalized [self pid]
    (when (and
            (empty? (swap! (.reps self) #(disj %1 %2) pid))
            (empty? @(.evals self))
            )
      (finalize/finalize self)
      )
    self
    )

  (evolveDone [self pid]
    (if @(.active self)
      (do
        (when (rand-nth [true false])
          (send pid reproducer/emigrateBest (rand-nth @(.migrantsDestination self)))
          )
        (send pid reproducer/evolve (:reproducersCapacity @(.pmConf self)))
        )
      (send pid finalize/finalize)
      )
    self
    )

  (evalDone [self pid n bs]
    (if @(.active self)
      (do
        (send (.manager self) islandManager/evalDone *agent* n bs)
        (let [
               evaluatorsCapacity (min (swap! (.evaluations self) #(- %1 %2) n)
                                    problem/evaluatorsCapacity
                                    )
               ]
          (if (> evaluatorsCapacity 0)
            (send pid evaluator/evaluate evaluatorsCapacity)
            ;            (poolManager/evaluationsDone self) ; acabamos pues se hicieron todas las evaluaciones (o más)
            (when @(.active self)
              ;      (send (.manager self) islandManager/endEvol (.getTime (Date.)))
              (send (.manager self) islandManager/numberOfEvaluationsReached *agent* bs)
              ;      (finalize/finalize self)
              (swap! (.active self) #(identity %2) false)
              )
            )
          )
        )
      (send pid finalize/finalize)
      )
    self
    )

  (sReps [self]
    (doseq [e @(.reps self)]
      (send e reproducer/evolve 0)
      )
    self
    )

  (sEvals [self]
    (doseq [e @(.evals self)]
      (send e evaluator/evaluate 0)
      )
    self
    )

  (deactivate! [self]
    (swap! (.active self) #(identity %2) false)
    (poolManager/finalizeAllWorkers self)
    self
    )

  (solutionReachedbyEvaluator [self [ind fit] pid]
    ;        (println "solutionReachedbyEvaluator" fit)
    (when @(.active self)
      ;      (send (.manager self) islandManager/endEvol (.getTime (Date.)))
      (send (.manager self) islandManager/solutionReached *agent* [ind fit])
      (send (.manager self) islandManager/poolManagerEnd *agent*)
      (swap! (.active self) #(identity %2) false)
      )
    self
    )

  ;  (evaluationsDone [self]
  ;    (when @(.active self)
  ;      ;      (send (.manager self) islandManager/endEvol (.getTime (Date.)))
  ;      (send (.manager self) islandManager/numberOfEvaluationsReached *agent*)
  ;      ;      (finalize/finalize self)
  ;      (swap! (.active self) #(identity %2) false)
  ;      )
  ;    self
  ;    )

  (evalEmpthyPool [self pid]
    (when @(.active self)
      ;    (println "esperando 50 msegs en evalEmpthyPool")
      (let [
             f (fn []
                 ;               (println "eval")
                 (send pid evaluator/evaluate (:evaluatorsCapacity @(.pmConf self)))
                 )
             ]

        (ShedulingUtility/send_after 100 f)
        )
      )
    self
    )

  (repEmpthyPool [self pid]
    (when @(.active self)
      (let [
             f (fn []
                 ;               (println "repr")
                 (send pid reproducer/evolve (:reproducersCapacity @(.pmConf self)))
                 )
             ]

        (ShedulingUtility/send_after 50 f)
        )
      )
    self
    )

  ;  (bestSolution [self]
  ;    (let [
  ;           evals (for [[ind [fit state]] @(.table self)
  ;                       :when (= state 2)]
  ;                   [ind fit]
  ;                   )
  ;           ]
  ;      (if (empty? evals)
  ;        [nil -1]
  ;        (reduce #(if (< (%1 1) (%2 1)) %2 %1) evals)
  ;        )
  ;
  ;      )
  ;    )

  finalize/Finalize
  (finalize [self]
    (send (.manager self) islandManager/poolManagerEnd *agent*)
    self
    )

  )