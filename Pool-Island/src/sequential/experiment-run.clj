(ns sequential.experiment-run)

(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/protocols.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/types.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/factories.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/evaluator.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/manager.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/poolManager.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/profiler.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/report.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/reproducer.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/experiment.clj")

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
         popSize 256
         chromosomeSize 128
         initPop [[] (genInitPop popSize chromosomeSize)]
         result (loop [population initPop]
                  (let [
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

    (println "The best solution is:" result)
    )

  :ok )

;(:require experiment)
;(:require evaluator)
;(:require pea)

(def bestSolution (atom -1))

(runSeqEA

  :genInitPop experiment/genInitPop

  :evaluatePopulation (fn [[alreadyEval nInds]]

                        (let [
                               toEvalEvaluated (for [i nInds]
                                                 (let [
                                                        cant (evaluator/maxOnes i)
                                                        ]
                                                   (when (= cant (count i))
                                                     (swap! bestSolution #(identity %2) cant)
                                                     )
                                                   [i cant]
                                                   )
                                                 )
                               ]
                          (into alreadyEval toEvalEvaluated)
                          )

                        )

  :findBestSolution (fn [_] @bestSolution)

  :terminationCondition #(not= @bestSolution -1)

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