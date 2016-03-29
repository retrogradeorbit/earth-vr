(ns earth-vr.renderer
  (:require [earth-vr.canvas :as c]))

;; plain stereo view renderer
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
        (.render renderer scene camera-r)))))

;;
;; port of javascript oculus rift renderer
;;

(def vertex-shader
"
varying vec2 vUv;

void main() {
    vUv = uv;
    gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
}
")

(def fragment-shader
"
uniform vec2 scale;
uniform vec2 scaleIn;
uniform vec2 lensCenter;
uniform vec4 hmdWarpParam;
uniform vec4 chromAbParam;
uniform sampler2D texid;
varying vec2 vUv;
void main()
{
  vec2 uv = (vUv*2.0)-1.0;
  vec2 theta = (uv-lensCenter)*scaleIn;
  float rSq = theta.x*theta.x + theta.y*theta.y;
  vec2 rvector = theta*(hmdWarpParam.x + hmdWarpParam.y*rSq + hmdWarpParam.z*rSq*rSq + hmdWarpParam.w*rSq*rSq*rSq);
  vec2 rBlue = rvector * (chromAbParam.z + chromAbParam.w * rSq);
  vec2 tcBlue = (lensCenter + scale * rBlue);
  tcBlue = (tcBlue+1.0)/2.0;
  if (any(bvec2(clamp(tcBlue, vec2(0.0,0.0), vec2(1.0,1.0))-tcBlue))) {
    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    return;}
  vec2 tcGreen = lensCenter + scale * rvector;
  tcGreen = (tcGreen+1.0)/2.0;
  vec2 rRed = rvector * (chromAbParam.x + chromAbParam.y * rSq);
  vec2 tcRed = lensCenter + scale * rRed;
  tcRed = (tcRed+1.0)/2.0;
  gl_FragColor = vec4(texture2D(texid, tcRed).r, texture2D(texid, tcGreen).g, texture2D(texid, tcBlue).b, 1);
}
")

(defmethod c/make-render-fn :oculus [options renderer scene camera]

)
