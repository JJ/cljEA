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

(ns problem)

(def terminationCondition :cantEvalsTerminationCondition )
;(def terminationCondition :fitnessTerminationCondition )
(def fitnessTerminationCondition maxOnes/fitnessTerminationCondition)
(def changeGen maxOnes/changeGen)
(def function maxOnes/function)
(def genInd maxOnes/genInd)

(ns experiment)

(require '[clojure.set :as cset])

(defn r1 [pprofiler pmanager]
  (let [
         ;         instanceFileName "../problems/uf100-01.cnf"
         popSize 56
         chromosomeSize 8
         evaluatorsCount 1
         evaluatorsCapacity 50 ; 20
         reproducersCount 1 ;10
         reproducersCapacity 50 ; 20
         evaluations 400

         conf {
                :evaluatorsCount evaluatorsCount
                :evaluatorsCapacity evaluatorsCapacity
                :reproducersCount reproducersCount
                :reproducersCapacity reproducersCapacity
                }

         mIslandManager (agent (islandManager/create pprofiler pmanager) ;                              :error-mode :continue
                          :error-handler pea/manager-error)

         p1 (agent (poolManager/create pprofiler mIslandManager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)
         ]

    (send pprofiler profiler/configuration conf 1)

    (send p1 poolManager/init (assoc conf
                                :population (problem/genInitPop popSize chromosomeSize)
                                )
      )

    (send p1 poolManager/migrantsDestination [p1])

    (send mIslandManager islandManager/init #{p1})

    (let [
           pools #{p1}
           poolsCount 1
           cociente (quot evaluations poolsCount)
           resto (rem evaluations poolsCount)
           [primeros ultimos] (split-at resto pools)
           ]

      (doseq [p primeros]
        (send p poolManager/initEvaluations (inc cociente))
        )

      (doseq [p ultimos]
        (send p poolManager/initEvaluations cociente)
        )
      )

    (send mIslandManager islandManager/start)
    )

  :ok )

(defn r2 [pprofiler pmanager]

  (let [
         popSize 256
         chromosomeSize 128

         conf {
                :evaluatorsCount 4
                :evaluatorsCapacity 50
                :reproducersCount 5
                :reproducersCapacity 50
                }

         mIslandManager (agent (islandManager/create pprofiler pmanager) ;                              :error-mode :continue
                          :error-handler pea/islandManager-error)

         p1 (agent (poolManager/create pprofiler mIslandManager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)

         p2 (agent (poolManager/create pprofiler mIslandManager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)

         ]

    (send pprofiler profiler/configuration conf 2)

    (send p1 poolManager/init (assoc conf
                                :population (problem/genInitPop popSize chromosomeSize)))
    (send p1 poolManager/migrantsDestination [p2])

    (send p2 poolManager/init (assoc conf
                                :population (problem/genInitPop popSize chromosomeSize)))
    (send p2 poolManager/migrantsDestination [p1])

    (send mIslandManager islandManager/init #{p1 p2})

    )

  :ok )