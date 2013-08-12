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

(load-file "./src/loaderFile.clj")

(ns sequential.experiment-run)

(use '[clojure.java.io :only (writer file)])
(import 'java.util.Date)

(defn
  ^{:ids {:npop "$P'$"}} ; Para la generacion de pseudocodigo Latex
  runSeqEA [& {:keys [
                       genInitPop
                       evaluatePopulation
                       findBestSolution
                       terminationCondition
                       selectParents
                       applyVariationOperators
                       selectNewPopulation
                       ]}]


  (let [
         initEvol (.getTime (Date.))
         initPop [[] (genInitPop problem/popSize problem/chromosomeSize)]
         result (loop [population initPop]
                  (let [
                         ;                         _ (println "PopSize:" (+ (count (nth population 0)) (count (nth population 1))))
                         iPopEvaluated (evaluatePopulation population)
                         bestSol (findBestSolution iPopEvaluated)
                         ]
                    (if (terminationCondition)
                      bestSol
                      (let [
                             npop (selectParents iPopEvaluated)
                             npop- (applyVariationOperators npop)
                             ]
                        (recur (selectNewPopulation iPopEvaluated npop-))
                        )
                      )
                    )
                  )
         ]

    ;    (println "The value of the best solution is:" result)
    result
    (- (.getTime (Date.)) initEvol)
    )

  ;  :ok
  )

;(:require experiment)
;(:require evaluator)
;(:require pea)

(def bestSolution (atom -1))
(def evaluations (atom 5000))

(def nRes (for [_ (range 10)]

            (runSeqEA
              :genInitPop problem/genInitPop

              :evaluatePopulation (fn [[alreadyEval nInds]]
                                    (let [
                                           sInds (case problem/terminationCondition
                                                   :fitnessTerminationCondition nInds
                                                   ; else
                                                   (let [
                                                          resX (take @evaluations nInds)
                                                          ]
                                                     (swap! evaluations #(- %1 %2) (count resX))
                                                     resX
                                                     )
                                                   )
                                           toEvalEvaluated (for [i sInds]
                                                             (let [
                                                                    fit (problem/function i)
                                                                    ]

                                                               (when (= problem/terminationCondition :fitnessTerminationCondition )
                                                                 (when (problem/fitnessTerminationCondition i fit)
                                                                   (swap! bestSolution #(identity %2) fit)
                                                                   )
                                                                 )

                                                               [i fit]
                                                               )
                                                             )
                                           ]
                                      (into alreadyEval toEvalEvaluated)
                                      )

                                    )

              :findBestSolution (fn [all]
                                  (case problem/terminationCondition
                                    :fitnessTerminationCondition @bestSolution
                                    ; else
                                    (do
                                      ((reduce #(if (< (%1 1) (%2 1)) %2 %1) all) 1)
                                      )
                                    )
                                  )

              :terminationCondition (fn []
                                      (case problem/terminationCondition
                                        :fitnessTerminationCondition (not= @bestSolution -1)
                                        ; else
                                        (do
                                          (= @evaluations 0)
                                          )
                                        )
                                      )

              :selectParents #(pea/extractSubpopulation % 30)
              :applyVariationOperators (fn [subpop]
                                         (let [
                                                [res evolResult]
                                                (pea/evolve
                                                  :subpop subpop
                                                  :parentsCount (quot (count subpop) 2)
                                                  )
                                                ]
                                           (if res
                                             evolResult
                                             [[] [] []]
                                             )
                                           )
                                         )

              :selectNewPopulation (fn [iPopEvaluated [noParents nInds bestParents]]
                                     (let [
                                            cantNews (reduce + (map count [noParents nInds bestParents]))
                                            cantOlds (count iPopEvaluated)
                                            [cant2DropFromIPopEvaluated cant2DropFromNoParents] (if (> cantOlds cantNews)
                                                                                                  [cantNews 0]
                                                                                                  [cantOlds (- cantNews cantOlds)]
                                                                                                  )
                                            alreadyEval (concat bestParents (drop cant2DropFromIPopEvaluated iPopEvaluated) (drop cant2DropFromNoParents noParents))
                                            ]
                                       [alreadyEval nInds]
                                       )
                                     )
              )
            )
  )

(with-open [w (writer (file "../../results/book2013/cljEA/seqResults.csv"))]
  (.write w "EvolutionDelay\n")
  (doseq [evolutionDelay nRes]
    ;                        (.write w (format "%1.6f,%2d,%3d,%4d,%5d,%6d,%7d \n" EvolutionDelay1 NumberOfEvals1 NEmig1 Ec Rc NIslands1 BestSol))
    (.write w (format "%1d\n" evolutionDelay))
    )
  )