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

(use '[clojure.java.io :only (writer file)])
(import 'java.util.Date)

(extend-type TManager
  manager/Manager

  (experimentEnd [self reportData]
    (println (format "Best fitness: %1d at %2d" (nth reportData 5) (.getTime (Date.))))

    (let [
           nRes (swap! (.results self) #(conj %1 %2) reportData)
           ]
      (if (empty? @(.instances self))
        (do
          (println "All ends!")

          (with-open [w (writer (file "../../results/book2013/cljEA/parResults.csv"))]
            (.write w "EvolutionDelay,NumberOfEvals,Emigrations,EvaluatorsCount,ReproducersCount,IslandsCount,BestSol\n")
            (doseq [[evolutionDelay nEmig conf nIslands numberOfEvals bestSol] nRes]
              (let [
                     Ec (:evaluatorsCount conf)
                     Rc (:reproducersCount conf)
                     ]
                ;                        (.write w (format "%1.6f,%2d,%3d,%4d,%5d,%6d,%7d \n" EvolutionDelay1 NumberOfEvals1 NEmig1 Ec Rc NIslands1 BestSol))
                (.write w (format "%1d,%2d,%3d,%4d,%5d,%6d,%7d \n"
                            evolutionDelay
                            numberOfEvals
                            nEmig
                            Ec Rc
                            nIslands
                            bestSol)
                  )
                )
              )
            )
          (ShedulingUtility/shutdown)
        )
      (do
        (manager/mkExperiment self)
        )
      )
    )
  self
  )

(mkExperiment [self]
  (let [
         insts @(.instances self)
         ]
    (when-not (empty? insts)
      (let [
             [exp name] (peek insts)
             ]
        (println (format "Doing experiment: %1s at %2d" name (.getTime (Date.))))
        (exp)
        (swap! (.instances self) #(identity %2) (pop insts))
        )

      )
    )
  self
  )

(session [self Funs]
  (swap! (.instances self) #(identity %2) Funs)
  (manager/mkExperiment self)
  self
  )

)