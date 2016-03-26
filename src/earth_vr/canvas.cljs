(ns earth-vr.canvas
  (:require [infinitelives.utils.events :as events]
            [infinitelives.utils.dom :as dom]
            [infinitelives.utils.console :refer [log]]
            [cljsjs.three]))

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
  (let [renderer (js/THREE.WebGLRenderer.)
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
