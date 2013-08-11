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
(require '[clojure.set])

(defn extractSubpopulation
  "returns: (seq [ind fitness])"
  [sels n]
  (let [
         res (sort pea/cmp2 sels)
         ]
    (take n res)
    )
  )

(defn bestParent [pop2r]
  (reduce #(if (> (%1 1) (%2 1)) %1 %2) pop2r)
  )

(defn mergeFunction [table subpop noParents
                     nInds bestParents poolSize]
  (let [
         p (concat noParents bestParents subpop)
         l1 (for [[i j] p] [i [j 2]])
         l2 (for [i nInds] [i [-1 1]])
         l3 (concat l1 l2)
         sub1 (into {} l3)
         table1 (apply dissoc table (keys sub1)) ; Remove the subpopulation already selected
         cant2Drop (- (count table1) (- poolSize (count sub1)))
         restOlds (apply dissoc table1 (take cant2Drop
                                         (for [[ind [_ state]] table1
                                               :when (= state 2)]
                                           ind
                                           )
                                         )
                    )
         more2Drop (- (+ (count sub1) (count restOlds)) poolSize)
         result (if (> more2Drop 0)
                  (apply dissoc restOlds (take more2Drop
                                           (for [[ind [_ state]] restOlds
                                                 :when (= state 1)]
                                             ind
                                             )
                                           )
                    )
                  restOlds
                  )
         ]
    (into result sub1)
    )
  )

(defn selectPop2Reproduce [population n]
  (let [
         tuple-3 (map #(nth population %)
                   (for [ind (range 3)] (rand-int (count population)))
                   )
         select1from3 (fn []
                        (reduce #(if (< (%1 1) (%2 1)) %2 %1) tuple-3) ; (OJO: Refactorizar)
                        )
         ]

    (for [_ (range (* 2 n))] (select1from3))
    )

  )

(defn parentsSelector
  "
  input:
   population: (seq a) 
   n: Int
  returns: array of (a, a)
  "
  [population n]

  (let [
         vp (vec population)
         positions (for [_ (range n)] [(rand-int (count vp)) (rand-int (count vp))])
         ]
    (for [[i j] positions] [(vp i) (vp j)])
    )

  )


(defn crossover
  "
  input: ((String, Int), (String, Int))
  returns: (String, String)
  "
  [[[ind1 f1] [ind2 f2]]]

  (let [
         indLength (count ind1)
         crossPoint (rand-int indLength)
         cross1 (split-at crossPoint ind1)
         cross2 (split-at crossPoint ind2)
         child1 (concat (nth cross1 0) (nth cross2 1))
         muttationPoint (rand-int indLength)
         mChild (split-at (dec muttationPoint) child1)
         m1 (nth mChild 0)
         m2 (nth mChild 1)
         m3 (rest m2)
         bit1 (problem/changeGen (first m2))
         result1 (concat m1 (conj m3 bit1))
         result2 (concat (nth cross2 0) (nth cross1 1))
         res1 (problem/genMerger result1)
         res2 (problem/genMerger result2)
         ]

    ;    (when (or
    ;          (> f1 125)
    ;          (> f2 125)
    ;          )
    ;      (when (= 128 (problem/function res1))
    ;        (println res1)
    ;        )
    ;      (when (= 128 (problem/function res2))
    ;        (println res2)
    ;        )
    ;      )
    ;
    [res1 res2]
    )
  )

(defn flatt [parents2flatt]
  (concat (for [[a _] parents2flatt] a) (for [[_ b] parents2flatt] b))
  )

(defn evolve [& {:keys [subpop parentsCount doWhenLittle] :or {doWhenLittle #()}}]

  (if (< (count subpop) 3)
    (do
      (doWhenLittle)
      [nil nil]
      )
    (do
      (let [

             pop2r (selectPop2Reproduce subpop parentsCount)
             parents2use (parentsSelector pop2r parentsCount)
             nIndsByPair (map crossover parents2use)
             nInds (concat (for [[I _] nIndsByPair] I) (for [[_ I] nIndsByPair] I))
             noParents (clojure.set/difference (set subpop) (set (flatt parents2use)))
             bestParents [(bestParent pop2r)]
             ]
        [:ok [noParents nInds bestParents]]
        )
      )
    )
  )

(extend-type TReproducer
  reproducer/Reproducer

  (evolve [self n]
    ;            (println "evolve")
    (let [
           subpop (extractSubpopulation
                    (for [[ind [fitness state]] @(.table @(.manager self))
                          :when (= state 2)]
                      [ind fitness]
                      )
                    n
                    )
           [res [noParents nInds bestParents]]
           (evolve
             :subpop subpop
             :parentsCount (quot n 2)
             :doWhenLittle (fn []
                             (send (.manager self) poolManager/repEmpthyPool *agent*)
                             )
             )
           ]
      (when res
        (send (.manager self)
          poolManager/updatePool
          (mergeFunction
            @(.table @(.manager self))
            subpop noParents
            nInds bestParents @(.poolSize @(.manager self))
            )
          )

        (send (.manager self) poolManager/evolveDone *agent*)
        (send (.profiler self) profiler/iteration nInds)
        )

      self
      )
    )

  (emigrateBest [self destination]
    (let [
           sels (for [[i [f s]] @(.table @(.manager self)) :when (= s 2)] [i f])
           ]

      (when (> (count sels) 0)
        (let [
               p (reduce pea/cmp1 sels)
               ]

          (send (.profiler self) profiler/migration p (.getTime (Date.)))
          (send destination poolManager/migration p)

          )
        )
      )
    self
    )

  finalize/Finalize
  (finalize [self]
    self
    )
  )
