(ns earth-vr.core
  (:require [cljsjs.three]
            [earth-vr.canvas :as c]
            [infinitelives.pixi.events :as e]
            )
  (:require-macros [cljs.core.async.macros :refer [go]])
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

(defonce canvas (c/init))

(def scene (:scene canvas))
(def camera (:camera canvas))

(defonce box (js/THREE.BoxGeometry. 400 400 400))
(defonce material (js/THREE.MeshBasicMaterial. #js {"color" 0xffffff "wireframe" true}))

(defonce mesh (js/THREE.Mesh. box material))

(defonce mainline
  (do
    (js/console.log "CANVAS:" canvas)
    (js/console.log "SCENE:" scene)
    (js/console.log "CAMERA:" camera)
    (js/console.log "BOX:" box)
    (js/console.log "MATERIAL:" material)

    (go
      (.add scene mesh)

      (loop [x 0 y 0]
        (set! (.-rotation.x mesh) x)
        (set! (.-rotation.y mesh) y)

        (<! (e/next-frame))

        (recur (+ 0.002 x) (+ 0.005 y))))))
