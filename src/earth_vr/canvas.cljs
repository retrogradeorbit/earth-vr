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

(defmulti make-render-fn (fn [opts _ _ _] (:render-style opts)))

(defmethod earth-vr.canvas/make-render-fn :normal [options renderer scene camera]
  #(.render renderer scene camera))

(defn init
  [& {:keys [render-style]
      :or {render-style :normal}
      :as options}]
  (let [{:keys [renderer canvas] :as world} (make)
        scene (js/THREE.Scene.)
        camera (let [cam (js/THREE.PerspectiveCamera.
                          75 (/ (.-innerWidth js/window) (.-innerHeight js/window)) 1 10000)]
                 (set! (.-position.z cam) 50)
                 (set! (.-position.y cam) 10)
                 (set! (.-rotation.x cam) -0.2)
                 cam)
        ]

    (.render renderer scene camera)

    (let [
          render-fn (make-render-fn options renderer scene camera)

          resize-fn (fn [width height]
                      (.setSize renderer width height)
                      (set! (.-aspect camera) (/ width height))
                      (.updateProjectionMatrix camera))


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
