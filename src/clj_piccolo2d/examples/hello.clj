(ns clj-piccolo2d.examples.hello
  (:use [clj-piccolo2d.core] :reload-all))

;; swing stuff
(def fr (javax.swing.JFrame.))
;; /swing stuff

(def cv (canvas))
(set-paint! cv :black)

(def txt (text "Hello World"))

(def r (rectangle 0 0 100 100))

(add! cv (translate 50 50
                    (rotate 10 r)))

(set-stroke! r 3.0 :butt :bevel)
(set-paint! r 125 0 0)
(set-stroke-paint! r :blue)

;;; Swing stuff
(.add fr cv)
(.setVisible fr true)
(.pack fr)
