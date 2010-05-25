(ns clj-piccolo2d.examples.hello
  (:use [clj-piccolo2d.core] :reload-all)
    (:use [overtone.live :only [scale snd] :rename {scale get-scale}]))

(def overpad nil)
(def piano-rel 0.3)
(defn start-piep [] nil)
(defn stop-piep [] nil)

;; swing stuff
(def fr (javax.swing.JFrame.))
;; /swing stuff
(def cv (canvas))
(set-paint! cv :white)
(doto cv
  (.setDefaultRenderQuality edu.umd.cs.piccolo.util.PPaintContext/HIGH_QUALITY_RENDERING)
  (.setAnimatingRenderQuality edu.umd.cs.piccolo.util.PPaintContext/HIGH_QUALITY_RENDERING)
  (.setInteractingRenderQuality edu.umd.cs.piccolo.util.PPaintContext/HIGH_QUALITY_RENDERING))

(defn slider [ update-fn ]
  (let [
        sl   (rectangle 0 0 30 250)
        knob (rectangle 0 0 30 30)
        lbl  (text "0.00")
        ]

    (doto sl
      (set-stroke! 1.0 :butt :bevel)
      (set-stroke-paint! 100 100 100)
      (add! knob)
      (add! (translate 0 255 lbl)))
      
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

                          (let [value (- (/ (- max-y  knob-y ) (- max-y 1)) 1)]

                            (if-not (nil? update-fn)
                              (update-fn value))

                            (set-text! lbl (format "%,.2f" value)))
                            
                          (handled! event))))
    sl))


(defn knob [ & [update-fn]]
  (let [c (ellipse 0 0 30 30)
        l (ref (line 15 10 15 0))
        lbl (text "0.00")
        val (ref 0.0)
        update-line-fn (fn []
                         (let [new-line (line 15 10 15 0)]
                           
                           (doto c
                             (.removeChild @l)
                             (add! new-line))
                           
                           (doto new-line
                             (.rotateAboutPoint (* (* -1 @val) (* 2 java.lang.Math/PI)) 15 15))

                           (dosync (ref-set l new-line))))]

    (doto c
      (set-paint! 170 250 170)
      (add! @l)
      (add! (translate 0 30 lbl)))

    (doto c
      (on-mouse-dragged event (let [delta (delta-relative-to event c)]

                                (let [new-val (+ @val (/ (height delta) 100))]

                                  (dosync (ref-set val 
                                                   (cond (> new-val 0.0) 0.0
                                                         (< new-val -1.0) -1.0
                                                         :else new-val)))

                                  (update-line-fn)

                                  (if-not (nil? update-fn) (update-fn @val))
                                  
                                  (set-text! lbl (format "%,.2f" @val)))
                                
                                (handled! event)
                                )))
    
    c))


(defn ill-button []
  (let [button (rectangle 0 0 30 30)
        led (rectangle 0 0 10 3)
        state (ref :off)]

    (doto led
      (set-paint! :black))
    (doto button
      (add! (translate 10 5 led))
      (on-mouse-clicked event
                        (dosync (ref-set state (cond (= @state :on) :off
                                                     (= @state :off) :on)))

                        (doto led
                          (set-paint! (cond (= @state :on) :red
                                            (= @state :off) :black)))
                        ))
    
    button))


(defn tangent [num] (let [bt (rectangle 0 0 40 250)]
                      (doto bt
                        (set-stroke! 2 :round :round)
                        (set-paint! :white)
                        (set-stroke-paint! :black)
                        (on-mouse-pressed event
                                          (set-paint! bt 255 200 200)
                                          )

                        (on-mouse-released event
                                           (set-paint! bt :white)
                                           (println (str "key: " num))
                                           (if-not (nil? overpad)
                                             (overpad 0 (nth (vec (get-scale :c :major)) num) 0.4 piano-rel)
                                             )
                                           )
                        )
                      bt))

(defn half-tangent [num] (let [bt (rectangle 0 0 30 150)]
                           (doto bt
                             (set-stroke! 2 :round :round)
                             (set-paint! :black)
                             (set-stroke-paint! :black)
                             
                             (on-mouse-pressed event
                                               (set-paint! bt 255 200 200)
                                               )
                             
                             (on-mouse-released event
                                                (set-paint! bt :black)
                                                (println (str "key: " num))
                                                (if-not (nil? overpad)
                                                  (overpad 0 (nth (vec (get-scale :c :major)) num) 0.4 piano-rel)
                                                  )
                                                )
                             )
                           bt))

(defn octave [offset] (let [kb (node)]

                        (add! kb (translate (* 0 40) 0 (tangent (+ offset 0)))) ;c
                        (add! kb (translate (* 1 40) 0 (tangent (+ offset 2)))) ;d
                        (add! kb (translate (* 2 40) 0 (tangent (+ offset 4)))) ;e
                        (add! kb (translate (* 3 40) 0 (tangent (+ offset 5)))) ;f
                        (add! kb (translate (* 4 40) 0 (tangent (+ offset 7)))) ;g
                        (add! kb (translate (* 5 40) 0 (tangent (+ offset 9)))) ;a
                        (add! kb (translate (* 6 40) 0 (tangent (+ offset 11)))) ;b

                        (add! kb (translate (+ 25 (* 0 40)) 0 (half-tangent (+ offset 1)))) ;c#
                        (add! kb (translate (+ 25 (* 1 40)) 0 (half-tangent (+ offset 3)))) ;d#
                        (add! kb (translate (+ 120 25 (* 0 40)) 0 (half-tangent (+ offset 6)))) ;f#
                        (add! kb (translate (+ 120 25 (* 1 40)) 0 (half-tangent (+ offset 8)))) ;g#
                        (add! kb (translate (+ 120 25 (* 2 40)) 0 (half-tangent (+ offset 10)))) ;a#
                        
                        kb))

(defn keyboard [] (let [kb (rectangle 0 0 (+ 100 (* 7 40 7)) 400)
                        gr (node)                          
                        lbl (text "Keyboard")]

                    (doto kb
                      (add! (translate 50 150 gr))
                      (set-stroke! 3 )
                      (set-paint! 230 230 230)
                      (add! (translate 30 30 lbl)))

                    (doto lbl
                      (.setFont (java.awt.Font. "Serif" java.awt.Font/PLAIN 40)))
                    (doall
                     (loop [i 0]
                       (add! gr (translate (* i (* 7 40)) 0 (octave (* (+ 1 i) 12)))) 
                       (if (< i 6)
                         (recur (+ i 1)))))
                    
                    kb))


(do
  
  (clear! cv)

  (add! cv (translate 0 560 (rotate -0.05 (keyboard))))

  (add! cv (translate 100 400 (scale 3 (knob (fn [val] (def piano-rel (+ 0.3 (* -5 val))))))))
  
  (add! cv (translate 0 140 (let [rec (rectangle 0 0 50 50)
                                  lbl (text "Start\nSinOsc")]

                              (add! rec (translate 5 12 lbl))
                              
                              (on-mouse-clicked rec event
                                                (start-piep))

                              rec)))

  (add! cv (translate 60 140 (let [rec (rectangle 0 0 50 50)
                                  lbl (text "Stop\nSinOsc")]

                              (add! rec (translate 5 12 lbl))
                              
                              (on-mouse-clicked rec event
                                                (stop-piep))

                              rec)))
  
  (add! cv (rotate 0.0
                   (let [n (node)]
                     (doall
                      (map #(add! n (translate (* % 40) 0 (knob))) (range 0 5))) n)))

    
  (add! cv (translate 0 100 (rotate 0.0
                                    (let [n (node)]
                                      (doall
                                       (map #(add! n (translate (* % 40) 0 (ill-button))) (range 0 5))) n))))


    
  (add! cv (translate 300 100
                      (rotate (* 10 (/ java.lang.Math/PI 180))
                              (let [pad (rectangle 0 0 250 320)]
                                  
                                (doto pad
                                  (set-paint! 230 230 255)
                                  (set-stroke-paint! 200 200 255)
                                  (set-stroke! 3 :round :round))
                                  
                                (doall
                                 (map #(add! pad
                                             (translate 30 30
                                                        (translate (* % 40) 0 (slider (fn [i] (snd "/c_set" 10 (+ 400 (* 1000 (* -1  i)))))))))
                                      (range 0 5))) 
                                  
                                pad))))
  
  (repaint! fr))

;;; Swing stuff

(.add fr cv)
(.setVisible fr true)
(.pack fr)
(.setSize fr 640 480)

 ;; load overtone stuff
(comment
  
  (use '[overtone.live :exclude [group node scale line octave snd]])

  (refer-ugens)
  (boot :internal)

  (defsynth overpad [out-bus 0 note 60 amp 0.4 rel 0.3]
    (let [freq (midicps note)
          env (env-gen (perc 0.01 rel) 1 1 0 1 :free)
          sig (apply + (sin-osc (/ freq 2)) (lpf (saw [freq (* freq 1.01)]) freq))
          audio (* amp env sig)]
      (out out-bus audio)))

  (defsynth foo [freq 440]
    (sin-osc freq))

  (defn start-piep []
    (def s (foo))
    (node-map-controls s "freq" 10))


  (defn stop-piep []
    (kill s))
  
  )


(comment

  (overpad 0 51 0.5 0.05)
  
  


  

  (node-map-controls s "freq" 10)

  (snd "/c_set" 10 400)

  (reset))
