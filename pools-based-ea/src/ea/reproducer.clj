(ns
  ea.reproducer
  (:gen-class))

(defn parents-selector [pop n]
  (let [
        nPar (count pop)
        f (fn []
            (let [
                  n1 (rand-int nPar)
                  n2 (rand-int nPar)
                  n3 (rand-int nPar)

                  i1 (nth pop n1)
                  i2 (nth pop n2)
                  i3 (nth pop n3)

                  ;n1 (rand-int nPar)
                  ;o (some #(when-not (= e %) %) (repeatedly #(nth pop (rand-int nPar)))) ; The first individual diferent to e
                  ]

              (if (< (nth i1 1) (nth i2 1))
                (if (< (nth i1 1) (nth i3 1))
                  [(nth i2 0) (nth i3 0)]
                  [(nth i2 0) (nth i1 0)]
                  )
                (if (< (nth i2 1) (nth i3 1))
                  [(nth i1 0) (nth i3 0)]
                  [(nth i2 0) (nth i1 0)]
                  )
                )

              ;[e o]
              )
            )
        ]
    (repeatedly n f)
    )
  )


(defn crossover [p]
  (let [
        [a b] p
        cPoint (inc (rand-int (dec (count a))))
        [a1 a2] (split-at cPoint a)
        [b1 b2] (split-at cPoint b)
        ]
    [(vec (concat a1 b2)) (vec (concat b1 a2))]
    )
  )

(defn change-gen [g] (if (= g 0) 1 0))

(defn mutate [ind]
  (let [
        pos (rand-int (count ind))
        [a b] (split-at pos ind)
        b1 (first b)
        b2 (rest b)
        ]
    (vec (concat a (conj b2 (change-gen b1))))
    )
  )


(defn reproduce [& {:keys [config iEvals]}]
  (if (> (count iEvals) 0)
    (let [
          lenSubPop (count iEvals)
          parents (parents-selector iEvals (quot lenSubPop 2))
          npInds (map crossover parents)
          l1 (for [[a _] npInds] a)
          l2 (for [[_ a] npInds] a)
          l3 (if (= (mod lenSubPop 2) 1) (list ((nth iEvals 0) 0)) '())
          nInds (concat l1 l2 l3)
          ]

      (map #(if (< (rand) (:PMutation config)) (mutate %) %) nInds)
      )
    '()
    )
  )
