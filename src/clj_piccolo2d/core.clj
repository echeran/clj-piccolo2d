(ns clj-piccolo2d.core
  (:import
   (java.awt BasicStroke
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
                            PUtil)))

(defmulti set-bounds! (fn [node x y w h] (type node)))
(defmethod set-bounds! PNode [node x y w h] (.setBounds node x y w h ))

(defmulti add! (fn [parent child] [(type parent) (type child)]))
(defmethod add! [PLayer PNode] [layer node] (.addChild layer node))
(defmethod add! [PNode PNode] [parent child] (.addChild parent child))

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

(defmulti set-paint! (fn [node color] [(type node) (type color)]))
(defmethod set-paint! [PNode Color] [node color] (.setPaint node color))
(defmethod set-paint! [PNode clojure.lang.Keyword] [node color] (set-paint! node (color-map color)))

(defn node [] (PNode.))
(defn layer [] (PLayer.))

(defmulti translate! (fn [node x y] (type node)))
(defmethod translate! PNode [node x y] (.translate node x y))

(defmulti scale! (fn [node value] (type node)))
(defmethod scale! PNode [node value] (.scale node value))

(defmulti rotate! (fn [node rotation] (type node)))
(defmethod rotate! PNode [node rotation] (.rotate node rotation))

(defmulti translate (fn [x y node] (type node)))
(defmethod translate PNode [x y node] (doto (PNode.) (translate! x y)))

(defmulti scale (fn [value node] (type node)))
(defmethod scale PNode [value node] (doto (PNode.) (scale! value)))

(defmulti rotate (fn [theta node] (type node)))
(defmethod rotate PNode [theta node] (doto (PNode.) (rotate! theta)))

