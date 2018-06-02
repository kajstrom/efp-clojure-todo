(ns efp-clojure-todo.core
  (:require
    [clojure.string :as s]
    [clojure.pprint :refer [print-table]]
    [cheshire.core :as cs]
    [monger.core :as mg]
    [monger.collection :as mc])
  (:import [org.bson.types ObjectId])
  (:gen-class))

(def coll "todos")
(def mongo-uri (System/getenv "TODO_MONGO_URI"))

(let [{:keys [conn db]} (mg/connect-via-uri mongo-uri)]
  (defn add-task [task]
    (mc/insert-and-return db coll (assoc task :_id (ObjectId.))))
  ;;(defn delete-note [id])
  (defn get-tasks []
    (mc/find-maps db coll))
  (defn disconnect [] (mg/disconnect conn)))

(def notes (atom (get-tasks)))

(defn parse-command [input]
  (let [splitted (s/split input #" ")
        command (first splitted)
        params (s/join " "(rest splitted) )]
    (case command
      "new" {:command :new :params params}
      "show" {:command :show :params params}
      "delete" {:command :delete :params (Integer/parseInt params)}
      "exit" {:command :exit :params params}
      {:command nil})))

(defn prompt-command []
  (let [input (read-line)]
    (parse-command input)))

(defn show []
  (print-table ["Id" "Task"] (map #(hash-map "Id" (:_id %) "Task" (:task %)) @notes)))

(defn new-task [task]
  (let [added-task (add-task {:task task})]
    (swap! notes conj added-task)))

(defn delete-task [id]
  (swap! notes (fn [c] (filter #(not= id (:id %)) c))))

(defn exit []
  (println "Goodbye...")
  (disconnect)
  (System/exit 0))

(defn command-loop[]
  (let [commmand (prompt-command)]
    (case (:command commmand)
      :show (show)
      :new (new-task (:params commmand))
      :delete (delete-task (:params commmand))
      :exit (exit)
      (println "Invalid command...")))
  (command-loop))

(defn -main
  [& args]
  (show)
  (println "'new [description]' creates a new task. 'show' lists tasks. 'delete [id]' deletes a task. 'exit' exits the program")
  (command-loop))