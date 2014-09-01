(ns pools-based-ea.core
  (:gen-class)
  (:require [clojure.data.json :as json])
  )

(use '[clojure.java.io :only (reader file)])

(require '(ea [problem :as problem]
              )
         )

(import '(java.util Date))

(defn -main
  [& args]
  (let [
        conf (with-open [r (reader (file "configMaxOnes.json"))]
               (json/read r :key-fn keyword)
               )
        obj (problem/create-maxOneProblem conf)
        ]

    (case (nth args 0)
      "seq" (println (nth (problem/runSeqCEvals obj) 1))
      "par" (do
              (let [
                    init-time (* 1000 (.getTime (Date.)))
                    ]
                (problem/runParCEvals obj (fn[sol evals]
                                            (let [
                                                  end-time (* 1000 (.getTime (Date.)))
                                                  res {:BestSol (nth sol 1) :NumberOfEvals evals :EvaluatorsCapacity (:EvaluatorsCapacity conf)
                                                       :ReproducersCapacity (:ReproducersCapacity conf)
                                                       :EvaluatorsCount (:EvaluatorsCount conf) :ReproducersCount (:ReproducersCount conf)
                                                       :EvolutionDelay  (- end-time init-time)}
                                                  ]
                                              (println (json/write-str res))
                                              (System/exit 1)
                                              )
                                            ))
                )
              )
      )
    )
  )


;; (require '(ea [evaluator :as evaluator]
;;               [reproducer :as reproducer]
;;               [problem :as problem]
;;               )
;;          )

;; (defn max-ones [lista]
;;   (count (filter #(= % 1) lista))
;;   )

;; (defn df [ind]
;;   (println "El fitness es:" (ind 1))
;;   )

;; (defn qf [val]
;;   (> val 4)
;;   )


;; (defn e []
;;   (let [p [[0 1 0 0] [1 1 1 1] [0 1 0 1] [0 1 1 0] [1 1 0 1]]
;;         conf (with-open [r (reader (file "configMaxOnes.json"))]
;;                (json/read r :key-fn keyword)
;;                )
;;         e (assoc conf :ff max-ones :df df :qf qf)
;;         ]
;;     ;    (print conf)
;;     ;    (println (evaluator/evaluate :config e :p2Eval p))
;;     (evaluator/evaluate :config e :p2Eval p)
;;     )
;;   )


;; (defn r []
;;   (let [
;;         iEvals (e)
;;         ]
;;     (reproducer/reproduce :iEvals iEvals)
;;     )
;;   )


;; (defn t2 []
;;   (let [
;;         pp (reproducer/enhanceParents '(:a :b :c :d :e))
;;         ]
;;     (reproducer/parentsSelector pp 20)
;;     )

;;   )

;; (defn t3 []
;;   (reproducer/crossover [[0 1 0 0] [1 1 0 0]])
;;   )

;; (defn t4 []
;;   (reproducer/mutate [0 1 0 0])
;;   )

;; (defn t5 []
;;   (let [
;;         conf (with-open [r (reader (file "configMaxOnes.json"))]
;;                (json/read r :key-fn keyword)
;;                )
;;         pop [[[0 1 0 0] 1] [[1 1 1 1] 4] [[0 1 0 1] 2] [[0 1 1 0] 2] [[1 1 0 1] 3]]
;;         ]
;;     (reproducer/reproduce :config conf :iEvals pop)
;;     )

;;   )


;; (defn t- []
;;   (let [
;;         conf (with-open [r (reader (file "configMaxOnes.json"))]
;;                (json/read r :key-fn keyword)
;;                )
;;         obj (problem/create-maxOneProblem conf)
;;         ]

;;     (println (nth (problem/runSeqCEvals obj) 1))
;;     )

;;   )


;; (defn work [w p c r]
;;   (let [

;;         f (future (w p))
;;         seg (fn []
;;               (let [v @f]
;;                 (println "doing")
;;                 (swap! c inc)
;;                 (if (< @c 6)
;;                   (work w v c r)
;;                   (deliver r v)
;;                   )
;;                 )
;;               )
;;         ]

;;     (future (seg))

;;     )
;;   )


;; (defn t6[]
;;   (let [
;;         cant (atom 0)
;;         res (promise)
;;         ]
;;     (work #(+ % 2) 1 cant res)
;;     (println "ya mande!")
;;     (work #(+ % 2) 1 cant res)
;;     (println "ya mande!")
;;     (println @res)
;;     )

;;   )


;; (defprotocol IA
;;   (m1 [_])
;;   (m2 [_])
;;   )


;; (def problem-abstract
;;   { :m1 (fn [self]
;;           (m2 self)
;;           )
;;     })


;; (defrecord A [n])

;; (extend A
;;   IA (assoc problem-abstract
;;        :m2 (fn [self]
;;              (println (.n self))
;;              )

;;        )
;;   )

;; (defn t7[]
;;   (let [obj (A. "Pepito")]
;;     (m1 obj)
;;     )
;;   )


;; (defn t []
;;   (let [
;;         conf (with-open [r (reader (file "configMaxOnes.json"))]
;;                (json/read r :key-fn keyword)
;;                )
;;         obj (problem/create-maxOneProblem conf)
;;         ]

;;     (problem/runParCEvals obj (fn[sol evals emigrs]
;;                                 (println (nth sol 1))
;;                                 ))

;;     ;(problem/runSeqCEvals obj)
;;     )

;;   )

;; (import '(pea.ds ReproducersPool))

;; (defn test-reproducers-pool []
;;   ;(defn t []
;;   (let [p (ReproducersPool. (comparator (fn [x y] (> (nth x 1) (nth y 1)))))]
;;     (.append p [[:a 13]])
;;     (.append p [[:b 15] [:c 14] [:d 12]])
;;     (println (.extractElements p 4))
;;     )
;;   )
