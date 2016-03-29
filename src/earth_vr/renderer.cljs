(ns earth-vr.renderer
  (:require [earth-vr.canvas :as c]))

(defmethod c/make-render-fn :stereo [options renderer scene camera]
  #(do
    (.updateMatrixWorld scene)
    (.updateMatrixWorld camera)

    (let [pos (js/THREE.Vector3.)
          quat (js/THREE.Quaternion.)
          scale (js/THREE.Vector3.)

          separation 3
          focal-length 15

          camera-l (js/THREE.PerspectiveCamera.)
          camera-r (js/THREE.PerspectiveCamera.)

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
  )

(defn foo [])
