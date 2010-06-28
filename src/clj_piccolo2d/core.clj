(ns clj-piccolo2d.core
  (:import
   (java.awt BasicStroke
             Stroke
             BorderLayout
             Color
             Point
             Dimension
             Font
             Insets
             RenderingHints
             Shape)
   (edu.umd.cs.piccolo PCamera
                       PCanvas
                       PInputManager
                       PLayer
                       PNode
                       PRoot)
   (edu.umd.cs.piccolo.activities PActivity 
                             PActivityScheduler 
                             PColorActivity 
                             PInterpolatingActivity 
                             PTransformActivity)
   (edu.umd.cs.piccolo.event PInputEventListener
                             PBasicInputEventHandler 
                             PDragEventHandler 
                             PDragSequenceEventHandler 
                             PInputEvent 
                             PInputEventFilter 
                             PPanEventHandler 
                             PZoomEventHandler)
   (edu.umd.cs.piccolo.nodes PHtmlView 
                             PImage 
                             PPath 
                             PText)
   (edu.umd.cs.piccolo.util PNodeFilter
                            PAffineTransform 
                            PBounds 
                            PDebug 
                            PDimension 
                            PObjectOutputStream 
                            PPaintContext 
                            PPickPath 
                            PStack 
                            PUtil)
   (edu.umd.cs.piccolox.pswing PSwingCanvas
                               PSwing)))

;; general
(derive java.lang.Float ::number)
(derive java.lang.Double ::number)
(derive java.lang.Integer ::number)
(derive clojure.lang.Keyword ::keyword)

(defn node [] (PNode.))
(defn layer [] (PLayer.))
(defn canvas [] (PCanvas.))

;; picking

(defn picked-node [event] (.getPickedNode event))
(defn handled! [event] (.setHandled event true))
(defn delta-relative-to [event node] (.getDeltaRelativeTo event node))

;; swing

(defn repaint! [frame] (.repaint frame))
(defn swing [canvas component] (PSwing. canvas component))
(defn swingcanvas [] (PSwingCanvas.))

;; bounds
(defmulti set-bounds! (fn [node x y w h] (type node)))
(defmethod set-bounds! PNode [node x y w h] (.setBounds node x y w h ))

;; add / remove /clear
(derive PNode ::addable)
(derive PPath ::addable)

(defmulti add! (fn [parent child] [(type parent) (type child)]))
(defmethod add! [PLayer ::addable] [layer node] (.addChild layer node))
(defmethod add! [PNode ::addable] [parent child] (.addChild parent child))
(defmethod add! [PCanvas ::addable] [parent child] (.addChild (.getLayer parent) child ))

(defmulti remove! (fn [parent child] [(type parent) (type child)]))
(defmethod remove! [PLayer ::addable] [layer node] (.removeChild layer node))
(defmethod remove! [PNode ::addable] [parent child] (.removeChild parent child))
(defmethod remove! [PCanvas ::addable] [parent child] (.removeChild (.getLayer parent) child ))

;;(defmethod remove! [PNode nil] [node] (.removeChild (.getParent node) node))

(defmulti clear! (fn [node] (type node)))
(defmethod clear! PNode [node] (.removeAllChildren node))
(defmethod clear! PLayer [layer] (.removeAllChildren layer))
(defmethod clear! PCanvas [canvas] (.removeAllChildren (.getLayer canvas)))

;; transformation
(defmulti translate! (fn [node x y] (type node)))
(defmethod translate! PNode [node x y] (.translate node x y))

(defmulti translate (fn [x y node] (type node)))
(defmethod translate PNode [x y node] (doto (PNode.) (translate! x y) (add! node)))

(defmulti atranslate! (fn [node x y] (type node)))
(defmethod atranslate! PNode [node x y]
  (.setTransform node nil)
  (.translate node x y))

(defmulti scale! (fn [node value] (type node)))
(defmethod scale! PNode [node value] (.scale node value))

(defmulti rotate! (fn [node rotation] (type node)))
(defmethod rotate! PNode [node rotation] (.rotate node rotation))



(defmulti scale (fn [value node] (type node)))
(defmethod scale PNode [value node] (doto (PNode.) (scale! value) (add! node)))

(defmulti rotate (fn [theta node] (type node)))
(defmethod rotate PNode [theta node] (doto (PNode.) (rotate! theta) (add! node)))

;; information

(defmulti width type)
(defmethod width PDimension [dimension] (.getWidth dimension))
(defmethod width PNode [node] (.getWidth node))

(defmulti height type)
(defmethod height PDimension [dimension] (.getHeight dimension))
(defmethod height PNode [node] (.getHeight node))

(defmulti x-offset type)
(defmethod x-offset PNode [node] (.getXOffset node))

(defmulti y-offset type)
(defmethod y-offset PNode [node] (.getYOffset node))

;; text
(defn text
  ([] (PText.))
  ([txt] (PText. txt)))

(defn set-text! [node text] (.setText node text))

(defn set-font! [node name style size]
  (let [st (cond (= style :plain) (Font/PLAIN)
                 (= style :bold) (Font/BOLD)
                 (= style :italic) (Font/ITALIC))]
    (.setFont node (Font. name st (Integer. size)))))

;; shapes
(defn ellipse [x y w h] (PPath/createEllipse x y w h))
(defn rectangle [x y w h] (PPath/createRectangle x y w h))
(defn line [x1 y1 x2 y2] (PPath/createLine x1 y1 x2 y2))

;; strokes
(def stroke-cap-map  {:butt   BasicStroke/CAP_BUTT 
                      :round  BasicStroke/CAP_ROUND
                      :square BasicStroke/CAP_SQUARE})

(def stroke-join-map {:bevel  BasicStroke/JOIN_BEVEL
                      :miter  BasicStroke/JOIN_MITER
                      :round  BasicStroke/JOIN_ROUND})

(defn stroke
  ([width] (BasicStroke. width))
  ([width cap join] (BasicStroke. width (stroke-cap-map cap) (stroke-join-map join))))

(derive Stroke ::stroke)

(defmulti set-stroke! (fn [node & args] [(type node)
                                         (type (nth args 0 nil))
                                         (type (nth args 1 nil))
                                         (type (nth args 2 nil))]))

(defmethod set-stroke! [PPath ::stroke nil nil] [node stroke] (.setStroke node stroke))
(defmethod set-stroke! [PPath ::number nil nil] [node width] (.setStroke node (stroke width)))
(defmethod set-stroke! [PPath ::number ::keyword ::keyword] [node width cap join] (.setStroke node (stroke width cap join)))

;; paint
(def color-map {:black      Color/BLACK     
                :blue       Color/BLUE      
                :cyan       Color/CYAN      
                :dark-gray  Color/DARK_GRAY  
                :gray       Color/GRAY      
                :green      Color/GREEN     
                :light-gray Color/LIGHT_GRAY 
                :magenta    Color/MAGENTA   
                :orange     Color/ORANGE    
                :pink       Color/PINK      
                :red        Color/RED       
                :white      Color/WHITE     
                :yellow     Color/YELLOW})

(defn color
  ([key] (Color. (color-map key)))
  ([r g b] (Color. r g b))
  ([r g b a] (Color. r g b a)))

(derive Color ::color)
(defmulti set-paint! (fn [node & args] [(type node)
                                        (type (nth args 0 nil))
                                        (type (nth args 1 nil))
                                        (type (nth args 2 nil))
                                        (type (nth args 3 nil))]))

(defmethod set-paint! [PCanvas ::color nil nil nil] [node color] (.setBackground node color))
(defmethod set-paint! [PCanvas ::keyword nil nil nil] [node key] (.setBackground node (color-map key)))
(defmethod set-paint! [PCanvas ::number ::number ::number nil] [node r g b] (.setBackground node (color r g b)))
(defmethod set-paint! [PCanvas ::number ::number ::number ::number] [node r g b a] (.setBackground node (color r g b a)))

(defmethod set-paint! [PPath ::color nil nil nil] [node color] (.setPaint node color))
(defmethod set-paint! [PPath ::keyword nil nil nil] [node key] (.setPaint node (color-map key)))
(defmethod set-paint! [PPath ::number ::number ::number nil] [node r g b] (.setPaint node (color r g b)))
(defmethod set-paint! [PPath ::number ::number ::number ::number] [node r g b a] (.setPaint node (color r g b a)))


(defmulti set-stroke-paint! (fn [node & args] [(type node)
                                        (type (nth args 0 nil))
                                        (type (nth args 1 nil))
                                        (type (nth args 2 nil))
                                        (type (nth args 3 nil))]))

(defmethod set-stroke-paint! [PPath ::color nil nil nil] [node color] (.setStrokePaint node color))
(defmethod set-stroke-paint! [PPath ::keyword nil nil nil] [node key] (.setStrokePaint node (color-map key)))
(defmethod set-stroke-paint! [PPath ::number ::number ::number nil] [node r g b] (.setStrokePaint node (color r g b)))
(defmethod set-stroke-paint! [PPath ::number ::number ::number ::number] [node r g b a] (.setStrokePaint node (color r g b a)))


(defmulti set-text-paint! (fn [node & args] [(type node)
                                        (type (nth args 0 nil))
                                        (type (nth args 1 nil))
                                        (type (nth args 2 nil))
                                        (type (nth args 3 nil))]))

(defmethod set-text-paint! [PText ::color nil nil nil] [node color] (.setTextPaint node color))
(defmethod set-text-paint! [PText ::keyword nil nil nil] [node key] (.setTextPaint node (color-map key)))
(defmethod set-text-paint! [PText ::number ::number ::number nil] [node r g b] (.setTextPaint node (color r g b)))
(defmethod set-text-paint! [PText ::number ::number ::number ::number] [node r g b a] (.setTextPaint node (color r g b a)))

                                      
;; events

(defmacro on-mouse-clicked [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseClicked [~event] ~@body))))

(defmacro on-mouse-entered [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseEntered [~event] ~@body))))

(defmacro on-mouse-exited [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseExited [~event] ~@body))))

(defmacro on-mouse-moved [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseMoved [~event] ~@body))))

(defmacro on-mouse-wheel-rotated [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseWheelRotated [~event] ~@body))))

(defmacro on-mouse-wheel-rotated-by-block [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseWheelRotatedByBlock [~event] ~@body))))

(defmacro on-mouse-pressed [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mousePressed [~event] ~@body))))

(defmacro on-mouse-dragged [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseDragged [~event] ~@body))))

(defmacro on-mouse-released [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (mouseReleased [~event] ~@body))))

(defmacro on-keyboard-focus-gained [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (keyboardFocusGained [~event] ~@body))))

(defmacro on-keyboard-focus-lost [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (keyboardFocusLost [~event] ~@body))))

(defmacro on-key-typed [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (keyTyped [~event] ~@body))))

(defmacro on-key-pressed [component event & body]
  `(. ~component addInputEventListener
      (proxy [PBasicInputEventHandler] []
        (keyPressed [~event] ~@body))))

