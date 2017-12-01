(ns exifixer.core
  (:require [clojure.java.io :as io])
  (:import [com.drew.imaging ImageMetadataReader ImageProcessingException]
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
  (try
    (when-let [l (pull-date f)]
      (when-not (= (.lastModified f) l)
        {:file f :timestamp l}))
    (catch ImageProcessingException ex (println (str "Ignoring " (.getName f) " because it's not a processable image.")))))

(defn find-images-with-exif
  [directory]
  (filter
   #(not (nil? %))
   (map
    build-data
    (find-files (file-seq (io/file directory))))))

(defn set-last-modified
  [{:keys [timestamp file]}]
  (when file
          (println (str "Changing " (.getName file) " from timestamp " (.lastModified file)
                        " to Exif timestamp " timestamp "."))
          (.setLastModified file timestamp)))

(defn fix-timestamps
  [directory]
  (doseq [image (find-images-with-exif directory)]
    (set-last-modified image)))

(defn -main
  [& args]
  (if (or (= (count args) 0)
          (not (.isDirectory (io/file (first args)))))
    (println "Usage: exifixer [directory]\ndirectory\tthe directory of images you want to set timestamps on")
    (fix-timestamps (first args))))
