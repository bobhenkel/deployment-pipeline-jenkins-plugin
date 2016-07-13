package com.pearson.deployment.config.bitesize

class BuildComponent implements Serializable {
  String name
  String version
  String os
  List<BuildDependency> dependencies
  BuildRepository repository
  List<? extends Map<String,String>> build
  List<? extends Map<String,String>> artifacts
} 