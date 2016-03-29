(ns earth-vr.core
  (:require [cljsjs.three]
            [earth-vr.canvas :as c]
            [earth-vr.renderer :as r]
            [infinitelives.pixi.events :as e]
            )
  (:require-macros [cljs.core.async.macros :refer [go]])
  )

(enable-console-print!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
)

(defonce canvas (c/init :render-style :stereo))

(defonce mainline
  (go
    (let [
          scene (:scene canvas)
          camera (:camera canvas)
          box (js/THREE.BoxGeometry. 400 400 400)
          geom (js/THREE.SphereGeometry. 20 48 48)
          material (js/THREE.MeshPhongMaterial.
                    #js {"color" 0xffffff
                         "specular" 0x808080
                         "shininess" 10.
                         "emissive" 0xffffff
                         })
          texture (js/THREE.ImageUtils.loadTexture "img/earth.jpg")
          mesh (js/THREE.Mesh. geom material)
          light (js/THREE.DirectionalLight. 0xffffff 1)
          ambient (js/THREE.AmbientLight. 0x101010 0.5)
          environ (js/THREE.SphereGeometry. 1000 16 16)
          environ-material (js/THREE.MeshBasicMaterial.)
          environ-mesh (js/THREE.Mesh. environ environ-material)
          clouds (js/THREE.SphereGeometry. 20.05 48 48)
          cloud-material (js/THREE.MeshPhongMaterial.
                          #js {"map" (js/THREE.ImageUtils.loadTexture "img/clouds.png")
                               "opacity" 0.8
                               "transparent" true
                               "depthWrite" false})
          cloud-mesh (js/THREE.Mesh. clouds cloud-material)
          ]

      (.add mesh cloud-mesh)

      (set! (.-map environ-material) (js/THREE.ImageUtils.loadTexture "img/stars.png"))
      (set! (.-side environ-material) js/THREE.BackSide)
      (set! (.-bumpMap material) (js/THREE.ImageUtils.loadTexture "img/bump.jpg"))
      (set! (.-bumpScale material) 0.4)
      (set! (.-specularMap material) (js/THREE.ImageUtils.loadTexture "img/specular.jpg"))
      (set! (.-specular material) (js/THREE.Color. "grey"))
      (set! (.-emissiveMap material) (js/THREE.ImageUtils.loadTexture "img/lights.jpg"))

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

        (recur x (+ 0.002 y) (+ 0.001 c))))))
