(ns exifixer.core
  (:require [clojure.java.io :as io])
  (:import [com.drew.imaging ImageMetadataReader]
           [com.drew.metadata.exif ExifSubIFDDirectory])
  (:gen-class))

(defn find-files
  "Finds file-like structures that are not hidden according to the OS."
  [dirlist]
  (filter #(and (.isFile %)
                (not (.isHidden %)))
          dirlist))

(defn pull-date
  "Pulls the Date/time property from EXIF data embedded in an image."
  [image-path]
  (with-open [image-stream (io/input-stream image-path)]
    (when-let [exif-dir (.getFirstDirectoryOfType (ImageMetadataReader/readMetadata image-stream) ExifSubIFDDirectory)]
      (when-let [date-tag (.getDate exif-dir ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)]
        (.getTime date-tag)))))

(defn build-data
  [f]
  (when-let [l (pull-date f)]
    (when-not (= (.lastModified f) l)
      {:file f
       :timestamp  (do
                     (println (str (.getName f) " has timestamp " (.lastModified f) " but Exif has timestamp " l "."))
                     l)})))

(defn find-images-with-exif
  [directory]
  (filter
   #(not (nil? %))
   (map
    build-data
    (find-files (file-seq (io/file directory))))))

(defn set-last-modified
  [{:keys [timestamp file]}]
  (.setLastModified file timestamp))

(defn fix-timestamps
  [directory]
  (map
   set-last-modified
   (find-images-with-exif directory)))

(defn -main
  [& args]
  (when (not= 1 (count args))
    (println "Usage: exifixer [directory]\ndirectory\tthe directory of images you want to set timestamps on"))
  (fix-timestamps (first args)))
