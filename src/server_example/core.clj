(ns server-example.core
  (:require [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [ring.middleware.params :as params]
            [ring.middleware.resource :as resource]
            [ring.middleware.file-info :as file-info] )
  (:import (java.io StringWriter)))

(defn on-init
  []
  (println "Initializing ..."))

(defn on-destroy
  []
  (println "Destroying ..."))

(defn simple-log-middleware
  [handler]
  (fn [{:keys [uri] :as request}]
    (println "Request path: " uri)
    (handler request)))

(defn not-found-middleware
  [handler]
  (fn [request]
    (or (handler request)
        {:body   (str "Not found: " (:uri request))
         :status 404})))

(defn exception-middleware-fn
  [handler request]
  (try (handler request)
    (catch Throwable e
      {:body   (str (.getMessage e) "\n" (apply str (interpose "\n" (.getStackTrace e))))
       :status 500})))

(defn exception-middleware
  [handler]
  (fn [request] (exception-middleware-fn handler request)))

(defn case-middleware-fn
  [handler request]
  (let [request (update-in request [:uri] string/lower-case)
        response (handler request)]
    (if (string? (:body response))
      (update-in response [:body] string/capitalize)
      response)))

(defn case-middleware
  [handler]
  (fn [request] (case-middleware-fn handler request)))

(defn echo-response-handler
  [request]
  (let [writer (StringWriter.)]
    (pprint request writer)
    {:body (str "request:\n" (.toString writer))}))

(defn version-handler
  [request]
  {:body "0.9.1"})

(defn hello-handler
  [{:keys [params] :as request}]
  {:body (str "Hello, " (get params "name" "world"))})

(defn erroring-handler
  [request]
  (throw (Exception. "I did a boo boo!"))
  {:body "error surely!"})

(defn routing-handler
  [request]
  (condp = (:uri request)
    "/favicon.ico" nil
    "/hello" (hello-handler request)
    "/echo" (echo-response-handler request)
    "/version" (version-handler request)
    "/error" (erroring-handler request)
    nil))

(def full-handler
  (-> routing-handler
      (resource/wrap-resource "public")
      file-info/wrap-file-info
      case-middleware
      not-found-middleware
      params/wrap-params
      exception-middleware
      simple-log-middleware))

