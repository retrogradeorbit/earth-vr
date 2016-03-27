(ns earth-vr.core
  (:require [cljsjs.three]
            [earth-vr.canvas :as c]
            [infinitelives.pixi.events :as e]
            )
  (:require-macros [cljs.core.async.macros :refer [go]])
  )

(enable-console-print!)

;; (println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(defonce canvas (c/init))

(defonce scene (:scene canvas))
(defonce camera (:camera canvas))

(defonce box (js/THREE.BoxGeometry. 400 400 400))

(defonce geom (js/THREE.SphereGeometry. 20 48 48))

;(defonce material (js/THREE.MeshBasicMaterial. #js {"color" 0xffffff "wireframe" true}))
(defonce material (js/THREE.MeshPhongMaterial.
                   #js {"color" 0xffffff
                        "specular" 0x808080
                        "shininess" 10.
                        "emissive" 0xffffff
;                        "emissiveIntensity" 0.2
                        }))
(defonce texture (js/THREE.ImageUtils.loadTexture "img/earth.jpg"))

(defonce mesh (js/THREE.Mesh. geom material))

(defonce light (js/THREE.DirectionalLight. 0xffffff 1))
(defonce ambient (js/THREE.AmbientLight. 0x101010 0.5))

(defonce environ (js/THREE.SphereGeometry. 500 16 16))
(defonce environ-material (js/THREE.MeshBasicMaterial.))
(set! (.-map environ-material) (js/THREE.ImageUtils.loadTexture "img/stars.png"))
(set! (.-side environ-material) js/THREE.BackSide)
(defonce environ-mesh (js/THREE.Mesh. environ environ-material))

(set! (.-bumpMap material) (js/THREE.ImageUtils.loadTexture "img/bump.jpg"))
(set! (.-bumpScale material) 0.4)
(set! (.-specularMap material) (js/THREE.ImageUtils.loadTexture "img/specular.jpg"))
(set! (.-specular material) (js/THREE.Color. "grey"))
(set! (.-emissiveMap material) (js/THREE.ImageUtils.loadTexture "img/lights.jpg"))

(defonce clouds (js/THREE.SphereGeometry. 20.05 48 48))
(defonce cloud-material (js/THREE.MeshPhongMaterial.
                   #js {"map" (js/THREE.ImageUtils.loadTexture "img/clouds.png")
                        "opacity" 0.8
                        "transparent" true
                        "depthWrite" false}))
(defonce cloud-mesh (js/THREE.Mesh. clouds cloud-material))
(.add mesh cloud-mesh)

(defonce mainline
  (go
    (js/console.log "CANVAS:" canvas)
    (js/console.log "SCENE:" scene)
    (js/console.log "CAMERA:" camera)
    (js/console.log "MATERIAL:" material)

    (.set (.-position light) -300 0 100)
    (.add scene light)
    (.add scene ambient)

    (set! (.-map material) texture)

    (.add scene mesh)


    (.add scene environ-mesh)

    (loop [x 0 y 0 c 0]
      (set! (.-rotation.x mesh) x)
      (set! (.-rotation.y mesh) y)
      (set! (.-rotation.y cloud-mesh) c)

      (<! (e/next-frame))

      (recur x (+ 0.002 y) (+ 0.001 c)))))
