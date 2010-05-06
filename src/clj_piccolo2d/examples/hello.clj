(ns clj-piccolo2d.examples.hello
  (:use [clj-piccolo2d.core] :reload-all))

(def n (node))
(def l (layer))

(set-bounds! n 0 0 100 80)

;(add! l n)
(set-paint! n :red)
