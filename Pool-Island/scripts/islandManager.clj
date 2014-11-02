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
    (swap! (.cierre self) #(identity %2) false)
    (swap! (.numberOfEvals self) #(identity %2) 0)

    self
    )

  (evalDone [self pid n bs]
    (when (some #{pid} @(.pools self))
      (swap! (.numberOfEvals self) #(+ %1 %2) n)
      (if (> (bs 1)
            (@(.bSolution self) 1)
            )
        (swap! (.bSolution self) #(identity %2) bs)
        )
      )
    self
    )

  (poolManagerEnd [self pid]
    ; Cuando llega el último reporte de finalizacion:
    (when
      (and
        (empty? (swap! (.pools self) #(disj % pid)))
        (not @(.cierre self))
        )

      (swap! (.cierre self) not)
      (finalize/finalize self)
      )
    self
    )

  (deactivate! [self]
    (doseq [p @(.pools self)]
      (send p poolManager/deactivate!)
      )
    self
    )

  (solutionReached [self _ sol]
    (when-not @(.endEvol self)
      (send (.profiler self) profiler/endEvol
        {
          :time (.getTime (Date.))
          :numberOfEvals @(.numberOfEvals self)
          :bestSolution (nth sol 1)
          }
        )
      (swap! (.endEvol self) #(identity %2) true)
      )
    (islandManager/deactivate! self)
    self
    )

  (numberOfEvaluationsReached [self pid bs]
    (if (> (bs 1) (@(.bSolution self) 1))
      (reset! (.bSolution self) bs)
      )
    ; Si no he acabado y pid es el ultimo pool:
    (when (and
            (empty? (swap! (.pools self) #(disj %1 %2) pid))
            ;            (not @(.endEvol self)) ; OJO!!!
            )

      (send (.profiler self) profiler/endEvol
        {
          :time (.getTime (Date.))
          :numberOfEvals @(.numberOfEvals self)
          :bestSolution (@(.bSolution self) 1)
          }
        )
      (swap! (.endEvol self) #(identity %2) true)
      (send pid poolManager/finalizeAllWorkers)
      )
    self
    )

;  (bestSolution [self]
;    (reduce #(if (< (%1 1) (%2 1)) %2 %1) @(.solutions self))
;    )

  finalize/Finalize
  (finalize [self]
    (profiler/experimentEnd @(.profiler self))
    self
    )

  )