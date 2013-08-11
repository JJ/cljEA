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

(ns finalize)

(defprotocol Finalize
  (finalize [self])
  )

(ns evaluator)

(defprotocol Evaluator
  (evaluate [self N])
  )

(ns islandManager)

(defprotocol IslandManager
  (start [self])
  (init [self ppools])
  (evalDone [self pid n])
  (poolManagerEnd [self pid])
  ;  (endEvol [self t])
  (solutionReached [self pid sol])
  (numberOfEvaluationsReached [self pid])
  (bestSolution [self])
  )

(ns poolManager)

(defprotocol PoolManager
  (init [self conf])
  (initEvaluations [self cant])
  (updatePool [self newPool])
  (add2Pool-Ind-Fit-State [self individuos])
  (migrantsDestination [self Dests])
  (migration [self ParIndividuoFitness])
  ;  (setPoolsManager [self Manager])
  (evaluatorFinalized [self pid])
  (reproducerFinalized [self pid])
  (evolveDone [self pid])
  (evalDone [self pid n])
  (sReps [self])
  (sEvals [self])
  (deactivate! [self])
  (solutionReachedbyEvaluator [self solution pid])
  (evaluationsDone [self])
  (evalEmpthyPool [self pid])
  (repEmpthyPool [self pid])
  (bestSolution [self])
  )

(ns manager)

(defprotocol Manager
;  (experimentEnd [self EvolutionDelay NEmig Conf NIslands NumberOfEvals])
  (experimentEnd [self reportData])
  (mkExperiment [self])
  (session [self Funs])
  )

(ns reproducer)

(defprotocol Reproducer
  (evolve [self n])
  (emigrateBest [self destination])
  )

(ns profiler)

(defprotocol Profiler
  (init [self rprt])
  (configuration [self nConf nNIslands])
  (migration [self [_ _] t])
  (initEvol [self t])
  (iteration [self population])
  (endEvol [self evolData])
  )