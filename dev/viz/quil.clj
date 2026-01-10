(ns viz.quil)

(defmacro ensure-quil!
  "Try to require `quil.core` in the calling namespace; if missing, use
   clojure.repl.deps/add-libs to install it and retry. Expands to nil.
   Because macros run with *ns* bound to the caller, the `require` will
   create the alias `q` in the caller namespace so callers can use `q/...`."
  []
  (try
    (require '[quil.core :as q])
    nil
    (catch Exception _
      (try
        (require '[clojure.repl.deps])
        #_{:clj-kondo/ignore [:unresolved-symbol]}
        (clojure.repl.deps/add-libs '{quil/quil {:mvn/version "4.3.1563"}})
        (require '[quil.core :as q])
        nil
        (catch Exception e
          (throw (ex-info "Quil not on classpath; add to deps or enable network for add-libs"
                          {:cause e})))))))

