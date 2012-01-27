(ns skkime_reg_maker_heroku.core
  (:use [compojure.core :only [defroutes GET POST]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as ring]
            [ring.middleware.multipart-params.temp-file :as tmp-file]
            [hiccup.form-helpers :as form] 
            [hiccup.page-helpers :as page]
            [clojure.java.io :as io]
            [skkime_reg_maker_heroku.readwrite :as rw])))

(def last-filename (atom "none"))

(defn index []
  (page/html5
   [:head
    [:title "SKKIME roma-kana registry entry maker."]]
   [:body
    [:div {:id "instruction"}   (str "Select input file name and submit. LastFile :" @last-filename)]
    [:form {:action "/upload" :method "POST" :enctype "multipart/form-data"}
     (form/label "upllabel" "Upload file name:")
     (form/file-upload "upload")
     [:br]
     (form/submit-button "UPLOAD")]]))

(defn result-page [input-file]
  (let [input (slurp input-file)
        output (rw/encode input-file)]
    (page/html5
     [:head
      [:title "SKKIME roma-kana registry entry maker."]]
     [:body
      [:div {:id "input" :name "input"}
       (form/text-area "input" input)
       (form/text-area "output" output)]])))

(defroutes routes
  (GET "/" [] (index))
  (POST "/" {params :params}
        (result-page (:filename (:upload params))))
  (route/resources "/")
  (route/files "/")
  (route/not-found "NOT FOUND"))

(def app
  (handler/site  routes))

(defn -main []
  (ring/run-jetty  app {:port 8080 :join? false}  ))


