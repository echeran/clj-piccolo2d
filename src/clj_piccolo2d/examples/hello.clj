(ns clj-piccolo2d.examples.hello
  (:use [clj-piccolo2d.core] :reload-all))

;; swing stuff
(def fr (javax.swing.JFrame.))
;; /swing stuff
(def cv (canvas))
(set-paint! cv :white)


(defn slider [ update-fn ]
  (let [
        sl   (rectangle 0 0 30 250)
        knob (rectangle 0 0 30 30)
        ]

    (doto sl
      (set-stroke! 1.0 :butt :bevel)
      (set-stroke-paint! 100 100 100)
      (add! knob))
      
    (doto knob
      (set-stroke! 2.0 :butt :bevel)
      (set-paint! 255 170 170)
      (on-mouse-dragged event
                      (let [node         (picked-node event)
                            knob-delta   (delta-relative-to event knob)
                            knob-h-delta (height knob-delta)
                            knob-y       (y-offset knob)
                            max-y        (- (height sl) (height knob))]
                        
                        (cond (> (+ knob-h-delta knob-y) (- max-y 1) ) (translate! node 0 (- max-y knob-y))
                              (< (+ knob-h-delta knob-y) 0)      (translate! node 0 0)
                              :else                              (translate! node 0 knob-h-delta))

                        
                        (update-fn (- (/ (- max-y  knob-y ) (- max-y 1)) 1))
                        
                        (handled! event))))
    {:node sl
     :update-fn nil
     }))

(comment)
(do

  (clear! cv)

  (add! cv
        (rotate 0.0
                (group
                 
                 (translate (* 0 40) 255 (text "00.11"))
                 (translate (* 0 40) 0 (:node (slider #(println (str "value " %) )) ))
                 
                 (translate (* 1 40) 255 (text "00.00"))
                 (translate (* 1 40) 0 (:node (slider nil)))
                  
                 (translate (* 2 40) 255 (text "00.00"))
                 (translate (* 2 40) 0 (:node (slider nil)))
                 
                 (translate (* 3 40) 255 (text "00.00"))
                 (translate (* 3 40) 0 (:node (slider nil)))

                 )))
  
  (repaint! fr))




;;; Swing stuff
(.add fr cv)
(.setVisible fr true)
(.pack fr)
