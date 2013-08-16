(ns evaluator)

(defn create [
               pidPoolManager ; agent
               pidProfiler ; agent
               ]
  ;(defrecord TEvaluator [manager profiler iterations])
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

    (pea.TPoolManager. pp
      (atom true) (atom 0) pprofiler
      pmanager (atom 0) (atom 0) (atom 0) (atom 0) (atom 0))
    )
  )

(ns islandManager)

(defn create [pprofiler pmanager]
  ;  (defrecord TManager
  ;    [pools ; set
  ;     profiler manager
  ;     endEvol numberOfEvals
  ;     solutions])
  (pea.TIslandManager.
    (atom #{}) pprofiler pmanager
    (atom 0) (atom 0) (atom []))
  )

(ns reproducer)

(defn create [pidPoolManager pidProfiler]
  ;(defrecord TReproducer [manager profiler])
  (pea.TReproducer. pidPoolManager pidProfiler)
  )

(ns profiler)

(defn create []
  ;  (defrecord TProfiler
  ;    [conf manager initEvol nIslands iterations emigrations])
  (pea.TProfiler. (atom 0) (atom 0) (atom 0) (atom 0) (atom 0) (atom 0))
  )

(ns manager)

(defn create [eProfiler]
  ;  (defrecord TReport
  ;    [results numberOfExperiments profiler instances])
  (pea.TManager. (atom []) (atom 0) eProfiler (atom []))
  )
