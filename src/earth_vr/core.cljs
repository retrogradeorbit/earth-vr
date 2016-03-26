(ns earth-vr.core
  (:require [cljsjs.three]
            [earth-vr.canvas :as c])
  )

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(def canvas (c/make))

(js/console.log "CANVAS:" canvas)
