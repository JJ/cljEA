(ns experiment)

(import 'sheduling.ShedulingUtility)

;(import 'java.io.FileReader)
;(import 'java.io.IOException)
;(import 'java.io.PrintStream)
;(import 'java.io.PrintWriter)
;(import 'java.io.FileOutputStream)
;
;(def fileStream (PrintStream. (FileOutputStream. "log.txt")))
;
;(System/setOut fileStream)
;(System/setErr fileStream)

(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/util.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/protocols.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/types.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/factories.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/evaluator.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/manager.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/poolManager.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/profiler.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/report.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/reproducer.clj")
(load-file "F:/Mis Documentos/PhD/src/cljEA/Pool-Island/src/experiment.clj")

(defn init []
  (ShedulingUtility/start)

  (let [
         eProfiler (agent (profiler/create) ;                        :error-mode :continue
                     :error-handler pea/profiler-error)

         eReport (agent (report/create eProfiler) ;                        :error-mode :continue
                   :error-handler pea/report-error)

         ]

    (send eProfiler profiler/init eReport)

    (send eReport report/session
      (vec (for [_ (range 3)] [#(r2 eProfiler eReport) "r2"]))

      ;      [
      ;        [#(r1 eProfiler eReport) "r1"]
      ;        ;        [#(r2 eProfiler eReport) "r2"]
      ;        ]

      )
    )

  :ok )

(init)