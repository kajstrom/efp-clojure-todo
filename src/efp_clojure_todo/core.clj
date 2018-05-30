(ns efp-clojure-todo.core
  (:require
    [clojure.string :as s]
    [clojure.pprint :refer [print-table]])
  (:gen-class))

(defonce notes (atom [{:id 1 :task "Do something"}]))

(defn parse-command [input]
  (let [splitted (s/split input #" ")
        command (first splitted)
        params (s/join " "(rest splitted) )]
    (case command
      "new" {:command :new :params params}
      "show" {:command :show :params params}
      "delete" {:command :delete :params (int params)}
      "exit" {:command :exit :params params}
      {:command nil})))

(defn prompt-command []
  (let [input (read-line)]
    (parse-command input)))

(defn show []
  (print-table ["Id" "Task"] (map #(hash-map "Id" (:id %) "Task" (:task %)) @notes)))

(defn exit []
  (println "Goodbye...")
  (System/exit 0))

(defn command-loop[]
  (let [commmand (prompt-command)]
    (case (:command commmand)
      :show (show)
      :exit (exit)
      (println "Invalid command...")))
  (command-loop))

(defn -main
  [& args]
  (println "'new [description]' creates a new task. 'show' lists tasks. 'delete [id]' deletes a task. 'exit' exits the program")
  (command-loop))
