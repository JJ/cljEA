(ns evaluator)

(defn create [
               pidPoolManager ; agent
               pidProfiler ; agent
               ]
  ;(defrecord TEvaluator [manager profiler])
  (pea.TEvaluator. pidPoolManager pidProfiler)
  )

(ns poolManager)

(defn create [pprofiler pmanager]
  ;  (defrecord TPoolManager
  ;    [table active migrantsDestination
  ;     profiler manager pmConf
  ;     evals reps poolSize])
  ;  (println "Creando poolmanagers")
  (let [
         pp (ref {})
         log (fn [key identity old new]

               (swap! pea/contador inc)

               (when (= @pea/contador 600)
                 (let [
                        st (pea/get-status new)
                        ]
                   (swap! pea/contador (fn [n nv]
                                         ;                                         (println "Cambiando de:" n "a" nv)
                                         nv) 0)
                   (println "Data evaluator:" (str st))
                   )
                 )
               )
         ]

    ;    (add-watch pp :log log)

    (pea.TPoolManager. pp (atom true) (atom 0) pprofiler pmanager (atom 0) (atom 0) (atom 0) (atom 0))
    )
  )

(ns reproducer)

(defn create [pidPoolManager pidProfiler]
  ;(defrecord TReproducer [manager profiler])
  ;  (println "creando repro con pmanager:" (class pidPoolManager))
  (pea.TReproducer. pidPoolManager pidProfiler)
  )

(ns profiler)

(defn create []
  ;  (defrecord TProfiler
  ;    [conf report initEvol nIslands iterations emigrations])
  (pea.TProfiler. (atom 0) (atom 0) (atom 0) (atom 0) (atom 0) (atom 0))
  )

(ns report)

(defn create [eProfiler]
  ;  (defrecord TReport
  ;    [results numberOfExperiments profiler instances])
  (pea.TReport. (atom []) (atom 0) eProfiler (atom []))
  )

(ns manager)

(defn create [pprofiler preport]
  ;  (defrecord TManager
  ;    [pools ; set
  ;     profiler report
  ;     endEvol numberOfEvals])
  (pea.TManager.
    (atom #{}) pprofiler preport
    (atom 0) (atom 0))
  )
