(defproject exifixer "0.1.0-SNAPSHOT"
  :description "Given an image directory, change the timestamps to match the date/time embedded in EXIF data."
  :url "https://github.com/youhavethewrong/exifixer"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.drewnoakes/metadata-extractor "2.10.1"]]
  :main ^:skip-aot exifixer.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
