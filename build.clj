(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]
            [borkdude.gh-release-artifact :as gh]))

(def lib 'com.eldrix/hermes)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def jar-basis (b/create-basis {:project "deps.edn"}))
(def uber-basis (b/create-basis {:project "deps.edn"
                                 :aliases [:run]}))
(def jar-file (format "target/%s-lib-%s.jar" (name lib) version))
(def uber-file (format "target/%s-%s.jar" (name lib) version))
(def github {:org    "wardle"
             :repo   "hermes"
             :tag    (str "v" version)
             :file   uber-file
             :sha256 true})

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (clean nil)
  (println "Building" jar-file)
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   version
                :basis     jar-basis
                :src-dirs  ["src"]
                :scm       {:url                 "https://github.com/wardle/hermes"
                            :tag                 (str "v" version)
                            :connection          "scm:git:git://github.com/wardle/hermes.git"
                            :developerConnection "scm:git:ssh://git@github.com/wardle/hermes.git"}})
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file  jar-file}))

(defn install
  "Installs pom and library jar in local maven repository"
  [_]
  (jar nil)
  (println "Installing" jar-file)
  (b/install {:basis     jar-basis
              :lib       lib
              :class-dir class-dir
              :version   version
              :jar-file  jar-file}))


(defn deploy
  "Deploy library to clojars.
  Environment variables CLOJARS_USERNAME and CLOJARS_PASSWORD must be set."
  [_]
  (println "Deploying" jar-file)
  (jar nil)
  (dd/deploy {:installer :remote
              :artifact  jar-file
              :pom-file  (b/pom-path {:lib       lib
                                      :class-dir class-dir})}))
(defn uber
  "Build an executable uberjar file for HTTP server and CLI tooling."
  [{:keys [out] :or {out uber-file}}]
  (println "Building uberjar:" out)
  (clean nil)
  (b/copy-dir {:src-dirs   ["resources"]
               :target-dir class-dir})
  (b/copy-file {:src    "cmd/logback.xml"
                :target (str class-dir "/logback.xml")})
  (b/compile-clj {:basis        uber-basis
                  :src-dirs     ["src" "cmd"]
                  :compile-opts {:elide-meta     [:doc :added]
                                 :direct-linking true}
                  :java-opts    ["-Dlogback.configurationFile=logback-build.xml"]
                  :class-dir    class-dir})
  (b/uber {:class-dir class-dir
           :uber-file out
           :basis     uber-basis
           :main      'com.eldrix.hermes.cmd.core}))

(defn release
  "Deploy release to GitHub. Requires valid token in GITHUB_TOKEN environmental
   variable."
  [_]
  (uber nil)
  (println "Deploying release to github")
  (gh/release-artifact github))