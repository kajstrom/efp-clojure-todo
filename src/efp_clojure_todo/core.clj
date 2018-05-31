(ns efp-clojure-todo.core
  (:require
    [clojure.string :as s]
    [clojure.pprint :refer [print-table]])
  (:gen-class))

(defonce notes (atom []))

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

(defn new-id
  "Creates (naively) a new id that is unique in the current notes collection"
  []
  (let [notes @notes]
    (if (not-empty notes)
      (+ 1 (apply max (map :id notes)))
      1)))

(defn show []
  (print-table ["Id" "Task"] (map #(hash-map "Id" (:id %) "Task" (:task %)) @notes)))

(defn new-task [task]
  (swap! notes conj {:id (new-id) :task task}))

(defn delete-task [id]
  (swap! notes (fn [c] (filter #(not= id (:id %)) c))))

(defn exit []
  (println "Goodbye...")
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
  (println "'new [description]' creates a new task. 'show' lists tasks. 'delete [id]' deletes a task. 'exit' exits the program")
  (command-loop))
