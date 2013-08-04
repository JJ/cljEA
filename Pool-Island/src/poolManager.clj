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

(require '[clojure.set])


(defn get-status [table]
  (let [
         evals (for [[ind [fitness state]] table
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


(defn adjustPool [table newEntries poolSize]
  (let [
         newInds (set (for [[i _] newEntries] i))
         tr (merge-with pea/merge-tables-function table newEntries)
         cant2Drop (- (count tr) poolSize)
         tableKeysSet (set (for [[ind [fitness state]] tr
                                 :when (= state 2)]
                             ind)
                        )

         alreadyEvaluatedkeys (clojure.set/difference tableKeysSet newInds)

         ]

    (if (>= (count alreadyEvaluatedkeys) cant2Drop)
      (let [
             alreadyEvaluated (for [k alreadyEvaluatedkeys] [k (nth (tr k) 0)])
             alreadyEvaluated2Trim (take cant2Drop (sort #(< (%1 1) (%2 1)) alreadyEvaluated))
             ]
        (apply dissoc tr (for [[i _] alreadyEvaluated2Trim] i))
        )
      (let [
             n1 (count alreadyEvaluatedkeys)
             tr1 (apply dissoc tr (for [k alreadyEvaluatedkeys] k))
             noEvalTableKeysSet (set (for [[ind [fitness state]] tr1
                                           :when (= state 1)]
                                       ind)
                                  )
             noEvaluatedkeys (clojure.set/difference noEvalTableKeysSet newInds)
             noEvaluated2Trim (take (- cant2Drop 1) noEvaluatedkeys)
             tr2 (apply dissoc tr1 (for [k noEvaluated2Trim] k))
             n2 (- (count tr2) poolSize)
             ]
        (if (> n2 0)
          (apply dissoc tr2 (take n2 (keys tr2)))
          tr2
          )
        )
      )

    ;    (merge-with pea/merge-tables-function table newEntries)
    )

  ;    (into table newEntries)
  ;  (def rres (merge-with pea/merge-tables-function table newEntries))
  ;  (println "Poniendo cosas en estado 2")
  ;  rres
  ;  result
  )


(extend-type TPoolManager
  poolManager/PoolManager
  ;agent que comparte un conjunto de individuos con
  ;agentes evaluadores y reproductores
  (init [self conf]
    (swap! (.pmConf self) #(identity %2)
      {
        :evaluatorsCount (:evaluatorsCount conf)
        :reproducersCount (:reproducersCount conf)
        :evaluatorsCapacity (:evaluatorsCapacity conf)
        :reproducersCapacity (:reproducersCapacity conf)
        })

    (swap! (.evals self) #(identity %2)
      (set (for [_ (range (:evaluatorsCount conf))]
             (agent (evaluator/create *agent* (.profiler self))
               ;               :error-mode :continue
               :error-handler pea/evaluator-error)
             ))
      )

    (swap! (.reps self) #(identity %2)
      (set (for [_ (range (:reproducersCount conf))]
             (agent (reproducer/create *agent* (.profiler self))
               ;               :error-mode :continue
               :error-handler pea/reproducer-error)
             ))
      )

    (swap! (.solutionReached self) #(identity %2) false)

    (let [
           population (:population conf)
           ]

      (swap! (.poolSize self) #(identity %2) (count population))

      (dosync
        (alter (.table self) #(identity %2)
          (zipmap population (for [_ population] [-1 1]))
          )
        )
      )
    self
    )

  (updatePool [self newPool]
    ;    (println "updatePool")

    (dosync
      (alter (.table self) #(identity %2) (adjustPool @(.table self) newPool @(.poolSize self)))
      )

    ;
    ;    (swap! pea/contador inc)
    ;
    ;    (if (= @pea/contador 50)
    ;      (do
    ;        (swap! pea/contador #(identity %2) 0)
    ;
    ;        (def st (get-status @(.table self)))
    ;
    ;        (println "eval:" (st 0))
    ;        (println "no eval:" (st 1))
    ;
    ;        (println "BestSol: " (st 2))
    ;        (println "WorstSol: " (st 3))
    ;        )
    ;      )



    self
    )


  (add2Pool-Ind-Fit-State [self individuos]
    (let [
           ndata (zipmap (for [[I _] individuos] I) (for [[_ FS] individuos] FS))
           ]
      (dosync
        (alter (.table self) #(identity %2) (adjustPool @(.table self) ndata @(.poolSize self)))
        )

      )

    ;    (println "acabando")

    self
    )

  (migrantsDestination [self dests]
    (swap! (.migrantsDestination self) #(identity %2) dests)
    self
    )

  (migration [self ParIndividuoFitness]
    (poolManager/add2Pool-Ind-Fit-State self [[(nth ParIndividuoFitness 0) [(nth ParIndividuoFitness 1) 2]]])
    self
    )
  ;
  ;  (setPoolsManager [self newManager]
  ;    (swap! (.manager self) #(identity %2) newManager)
  ;    self
  ;    )

  (evaluatorFinalized [self pid]
    (let [
           nEvals (swap! (.evals self) #(disj %1 %2) pid)
           ]
      (when (and
              (empty? @(.reps self))
              (empty? nEvals)
              )
        (send pid evaluator/finalize)
        )
      )

    self
    )

  (reproducerFinalized [self pid]
    (let [
           nReps (swap! (.reps self) #(disj %1 %2) pid)
           ]
      (when (and
              (empty? nReps)
              (empty? @(.evals self))
              )
        (send pid reproducer/finalize)
        )
      )

    self
    )

  (evolveDone [self pid]

    (if @(.solutionReached self)
      (send pid reproducer/finalize)
      (do
        (when (= (rem (rand-int 100) 2) 0)
          (send pid reproducer/emigrateBest (rand-nth @(.migrantsDestination self)))
          )
        (send pid reproducer/evolve (:reproducersCapacity @(.pmConf self)))
        )
      )
    self
    )

  (evalDone [self pid]

    (if @(.solutionReached self)
      (send pid evaluator/finalize)
      (do
        (send (.manager self) manager/evalDone *agent*)
        (send pid evaluator/evaluate (:evaluatorsCapacity @(.pmConf self)))
        )
      )
    self
    )

  (sReps [self]
    (doseq [e @(.reps self)]
      (send e reproducer/evolve (:reproducersCapacity @(.pmConf self)))
      )
    self
    )

  (sEvals [self]
    (doseq [e @(.evals self)]
      (send e evaluator/evaluate (:evaluatorsCapacity @(.pmConf self)))
      )
    self
    )

  (solutionReachedbyAny [self]
    (when-not @(.solutionReached self)
      (doseq [e @(.reps self)]
        (send e reproducer/finalize))
      (doseq [e @(.evals self)]
        (send e evaluator/finalize))
      (swap! (.solutionReached self) #(identity %2) true)

      )
    self
    )

  (solutionReachedbyEvaluator [self pid]
    ;    (println "solutionReachedbyEvaluator")
    (when-not @(.solutionReached self)
      (send (.manager self) manager/endEvol (.getTime (Date.)))
      (send (.manager self) manager/solutionReachedByPoolManager *agent*)
      (swap! (.solutionReached self) #(identity %2) true)
      )
    self
    )

  (evalEmpthyPool [self pid]
    ;    (println "esperando 50 msegs en evalEmpthyPool")
    (let [
           f (fn []

               ;               (println "eval")
               (send pid evaluator/evaluate (:evaluatorsCapacity @(.pmConf self)))

               )
           ]

      (ShedulingUtility/send_after 100 f)
      )
    self
    )

  (repEmpthyPool [self pid]

    (let [
           f (fn []

               ;               (println "repr")
               (send pid reproducer/evolve (:reproducersCapacity @(.pmConf self)))

               )
           ]

      (ShedulingUtility/send_after 50 f)
      )
    self
    )

  (finalize [self]
    (send (.manager self) manager/poolManagerEnd *agent*)
    self
    )

  )