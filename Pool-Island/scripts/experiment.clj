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

(ns experiment)

(defn r1 [pprofiler pmanager]
  (let [
         conf {
                :evaluatorsCount problem/evaluatorsCount
                :evaluatorsCapacity problem/evaluatorsCapacity
                :reproducersCount problem/reproducersCount
                :reproducersCapacity problem/reproducersCapacity
                }

         mIslandManager (agent (islandManager/create pprofiler pmanager) ;                              :error-mode :continue
                          :error-handler pea/manager-error)

         p1 (agent (poolManager/create pprofiler mIslandManager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)
         ]

    (send pprofiler profiler/configuration conf 1)

    (send p1 poolManager/init (assoc conf
                                :population (problem/genInitPop problem/popSize problem/chromosomeSize)
                                )
      )

    (send p1 poolManager/migrantsDestination [p1])

    (send mIslandManager islandManager/init #{p1})

    (let [
           pools #{p1}
           poolsCount (count pools)
           cociente (quot problem/evaluations poolsCount)
           resto (rem problem/evaluations poolsCount)
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
         evaluations problem/evaluations

         conf {
                :evaluatorsCount problem/evaluatorsCount
                :evaluatorsCapacity problem/evaluatorsCapacity
                :reproducersCount problem/reproducersCount
                :reproducersCapacity problem/reproducersCapacity
                }

         mIslandManager (agent (islandManager/create pprofiler pmanager) ;                              :error-mode :continue
                          :error-handler pea/manager-error)

         p1 (agent (poolManager/create pprofiler mIslandManager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)

         p2 (agent (poolManager/create pprofiler mIslandManager) ;                        :error-mode :continue
              :error-handler pea/poolManager-error)
         ]

    (send pprofiler profiler/configuration conf 2)

    (send p1 poolManager/init (assoc conf
                                :population (problem/genInitPop problem/popSize problem/chromosomeSize)
                                )
      )

    (send p2 poolManager/init (assoc conf
                                :population (problem/genInitPop problem/popSize problem/chromosomeSize)
                                )
      )

    (send p1 poolManager/migrantsDestination [p2])
    (send p2 poolManager/migrantsDestination [p1])

    (send mIslandManager islandManager/init #{p1 p2})

    (let [
           pools #{p1 p2}
           poolsCount (count pools)
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