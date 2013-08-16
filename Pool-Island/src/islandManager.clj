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

(extend-type TIslandManager
  islandManager/IslandManager

  (start [self]

    (send (.profiler self) profiler/initEvol (.getTime (Date.)))

    (doseq [p @(.pools self)]
      ;        (send p poolManager/setPoolsManager *agent*)
      (send p poolManager/sReps)
      (send p poolManager/sEvals)
      )
    self
    )

  (init [self ppools]
    (swap! (.pools self) #(identity %2) ppools)

    (swap! (.endEvol self) #(identity %2) false)
    (swap! (.numberOfEvals self) #(identity %2) 0)
    self
    )

  (evalDone [self pid n]
    (when (some #{pid} @(.pools self))
      (swap! (.numberOfEvals self) #(+ %1 %2) n)
      )
    self
    )

  (poolManagerEnd [self pid]
    (send pid finalize/finalize)
    ; Cuando llega el último reporte de finalizacion:
    (when (empty? (swap! (.pools self) #(disj % pid)))
      (finalize/finalize self)
      )
    self
    )

  (solutionReached [self _ sol]
    ;    (println "solutionReachedByPoolManager")
    (if-not @(.endEvol self)
      (do
        (send (.profiler self) profiler/endEvol
          {
            :time (.getTime (Date.))
            :numberOfEvals @(.numberOfEvals self)
            :bestSolution (nth sol 1)
            }
          )
        (swap! (.endEvol self) #(identity %2) true)
        )
      )
    (doseq [p @(.pools self)]
      (send p poolManager/deactivate!)
      (finalize/finalize self)
      )
    self
    )

  (numberOfEvaluationsReached [self pid]
    (swap! (.solutions self) #(conj %1 %2) (poolManager/bestSolution @pid))
    ; Si no he acabado y pid es el ultimo pool:
    (when (and
            (not @(.endEvol self))
            (empty? (swap! (.pools self) #(disj %1 %2) pid))
            )
      (send (.profiler self) profiler/endEvol
        {
          :time (.getTime (Date.))
          :numberOfEvals @(.numberOfEvals self)
          :bestSolution ((islandManager/bestSolution self) 1)
          }
        )
      (swap! (.endEvol self) #(identity %2) true)
      )

    ;    (if-not @(.endEvol self)
    ;           (do
    ;             (send (.profiler self) profiler/endEvol
    ;               {
    ;                 :time (.getTime (Date.))
    ;                 :numberOfEvals @(.numberOfEvals self)
    ;                 :bestSolution ((islandManager/bestSolution self) 1)
    ;                 }
    ;               )
    ;             (swap! (.endEvol self) #(identity %2) true)
    ;             )
    ;           )
    ;    (doseq [p @(.pools self)]
    ;      (send p poolManager/deactivate!)
    ;      (finalize/finalize self)
    ;      )
    self
    )

  (bestSolution [self]
    (reduce #(if (< (%1 1) (%2 1)) %2 %1) @(.solutions self))
    )

  finalize/Finalize
  (finalize [self]
    self
    )

  )