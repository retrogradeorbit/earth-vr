(ns earth-vr.canvas
  (:require [infinitelives.utils.events :as events]
            [infinitelives.utils.dom :as dom]
            [infinitelives.utils.console :refer [log]]
            [cljsjs.three]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def canvas-store (atom {}))

(defn add!
  ([key canvas]
   (swap! canvas-store assoc key canvas))
  ([canvas]
   (add! :default canvas)))

(defn set-default-once!
  [canvas]
  (when-not (:default @canvas-store)
    (add! canvas)))

(defn remove!
  ([key]
   (swap! canvas-store dissoc key)))

(defn get
  ([key]
   (key @canvas-store))
  ([]
   (:default @canvas-store)))

(defn make
  "make a new THREE canvas, or initiallise THREE with an existing
  canvas"

  []
  (let [renderer  (js/THREE.WebGLRenderer.)

        actual-canvas (.-domElement renderer)]
    (.setSize renderer
              (.-innerWidth js/window)
              (.-innerHeight js/window))
    (dom/set-style! actual-canvas
                    :left 0
                    :top 0
                    :position "absolute")
    (dom/append! (.-body js/document) actual-canvas)
    {
     :renderer renderer
     :canvas actual-canvas})

  )

(defn init
  []
  (let [{:keys [renderer canvas] :as world} (make)
        scene (js/THREE.Scene.)
        camera (let [cam (js/THREE.PerspectiveCamera.
                          75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 1 10000)]
                 (set! (.-position.z cam) 50)
                 (set! (.-position.y cam) 10)
                 (set! (.-rotation.x cam) -0.2)
                 cam)

        ;controls (js/THREE.OrbitControls. camera (.-domElement renderer))

        separation 3
        focal-length 15

        camera-l (js/THREE.PerspectiveCamera.)
        camera-r (js/THREE.PerspectiveCamera.)

        ]

    (.render renderer scene camera)

    (let [
          render-fn-plain #(.render renderer scene camera)

          render-fn
          #(do
             (.updateMatrixWorld scene)
             (.updateMatrixWorld camera)

             (let [pos (js/THREE.Vector3.)
                   quat (js/THREE.Quaternion.)
                   scale (js/THREE.Vector3.)
                   ]
               (.decompose (.-matrixWorld camera) pos quat scale)

               (let [
                     width (.-innerWidth js/window)
                     half-width (/ width 2)
                     height (.-innerHeight js/window)

                     fov (->
                          (.-fov camera)
                          js/THREE.Math.degToRad
                          (* 0.5)
                          Math/tan
                          Math/atan
                          (* 2)
                          js/THREE.Math.radToDeg)
                     ndfl (/ (.-near camera) focal-length)
                     half-focal-height (-> fov
                                           js/THREE.Math.degToRad
                                           (* 0.5)
                                           Math/tan
                                           (* focal-length))
                     half-focal-width (* half-focal-height (.-aspect camera) 0.5)
                     top (* half-focal-height ndfl)
                     bottom (- top)
                     inner-factor (/ (+ half-focal-width (/ separation 2))
                                     (* 2.0 half-focal-width))
                     outer-factor (- 1.0 inner-factor)

                     outer (* half-focal-width 2.0 ndfl outer-factor)
                     inner (* half-focal-width 2.0 ndfl inner-factor)]

                 ;; left
                 (-> camera-l
                     .-projectionMatrix
                     (.makeFrustum (- outer) inner bottom top
                                   (.-near camera) (.-far camera)))

                 (-> camera-l .-position (.copy pos))
                 (-> camera-l .-quaternion (.copy quat))
                 (-> camera-l (.translateX (- (/ separation 2.0))))

                 ;; right
                 (-> camera-r
                     .-projectionMatrix
                     (.makeFrustum (- inner) outer bottom top
                                   (.-near camera) (.-far camera)))

                 (-> camera-r .-position (.copy pos))
                 (-> camera-r .-quaternion (.copy quat))
                 (-> camera-r (.translateX (- (/ separation 2.0))))

                 (set! (.-autoClear renderer) false)

                 (.setViewport renderer 0 0 width height)
                 (.clear renderer)

                 (.setViewport renderer 0 0 half-width height)
                 (.render renderer scene camera-l)

                 (.setViewport renderer half-width 0 half-width height)
                 (.render renderer scene camera-r))))

          resize-fn (fn [width height]
                      (.setSize renderer width height)
                      (set! (.-aspect camera) (/ width height))
                      (.updateProjectionMatrix camera)
                      )

          expand-fn #(resize-fn (.-innerWidth js/window)
                                (.-innerHeight js/window))

          resizer-loop
          (when true (let [c (events/new-resize-chan)]
                       (go (while true
                             (let [[width height] (<! c)]
                               (resize-fn width height)
                               (render-fn))))))
          ]
      ;; setup render loop
      (defn render []
        (events/request-animation-frame render)
        (render-fn))

      (render)

      (let [canvas (into world
                         {:scene scene
                          :camera camera
                          :render-fn render-fn
                          :resize-fn resize-fn
                                        ;:fullscreen-fn fullscreen-fn
                          :expand-fn expand-fn})]
        (set-default-once! canvas)
        canvas))))
