(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/util.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/protocols.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/types.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/factories.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/problems.clj")

(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/evaluator.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/islandManager.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/poolManager.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/profiler.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/manager.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/reproducer.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/experiment.clj")

(ns sequential.experiment-run)

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

    (println "The value of the best solution is:" result)
    )

  :ok )

;(:require experiment)
;(:require evaluator)
;(:require pea)

(def bestSolution (atom -1))
(def evaluations (atom 400))

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