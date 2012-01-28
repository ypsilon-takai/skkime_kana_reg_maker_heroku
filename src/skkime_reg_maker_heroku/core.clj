(ns skkime_reg_maker_heroku.core
  (:use [compojure.core :only [defroutes GET POST]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as ring]
            [ring.middleware.multipart-params.temp-file :as tmp-file]
            [hiccup.form-helpers :as form] 
            [hiccup.page-helpers :as page]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [skkime_reg_maker_heroku.readwrite :as rw]))

(defn page-skel [contents]
  (page/html5
   [:head
    [:title "SKKIME roma-kana registry entry maker."]
    (page/include-css "/css/style.css")]
   [:body
    [:div {:id "page"}
     [:div {:id "head"}
      [:img {:src "/img/yplogo.png" :height 100}]
      [:h1 "SKKIME roma-kana setting generator"]]
     [:div {:id "menu"}
      [:ul
       [:li [:a {:href "/"} "Input"]]]]
     [:div {:id "submenu"}
      [:p "Link"]
      [:ul
       [:li [:a {:href "https://github.com/ypsilon-takai/skkime_kana_reg_maker_heroku"} "Source code(Github)"]]]]
     contents]]))

(defn input-page []
  (page/html5
   [:div {:id "main"}
    [:h2 "Select input file and submit."]
    [:form {:action "/upload" :method "POST" :enctype "multipart/form-data"}
     (form/label "upllabel" "Upload file name:")
     (form/file-upload "upload")
     [:br]
     (form/submit-button "UPLOAD")
     [:br]
     [:br]
     [:br]
     [:br]
     [:br]
     
     [:h2 "使いかた"]
     [:p
      "変換ファイルを指定してください。"[:br]
      "ファ`イルのフォーマットは"[:br]
      [:ul
       [:li "1行に1エントリ"]
       [:li "[入力記号列]|[出力ひらがな]|[出力カタカナ]|[残る文字列] のフォーマットです。 "]      [:li "詳しくは、[:a {:href /sample.txt} このファイルを] を参考にしてください。"]
       [:br]
       "※ 注意 ファイルのフォーマットは必ずUTF-8にしてください。それ以外のファイルは変換できません。"]]]]))

(defn result-page [parm]
  (page/html5
   [:h1 "Your input and result."]
   [:div {:id "output"}
    (form/text-area {:id "inputdata" :rows 30} "input"
                    (:input-data parm))
    (form/text-area {:id "outputdata" :rows 30} "output"
                    (str (string/join "\\\n"  (:output-data parm))
                         "\\\n"))]))

(defroutes routes
  (GET "/" [] (page-skel (input-page)))
  (POST "/upload" {params :params}
        (let [tempfile (:tempfile (:upload params))
              input (slurp tempfile)
              output (rw/encode tempfile)]
          (page-skel (result-page {:input-data input :output-data output}))))
  (route/resources "/")
  (route/files "/")
  (route/not-found "NOT FOUND"))

(def app
  (handler/site routes))

;; for on the fly
;; (use 'ring.util.serve)
;; (serve app)
;; (stop-server)

(defn -main [port]
  (ring/run-jetty app {:port (Integer/parseInt port ) :join? false}))

