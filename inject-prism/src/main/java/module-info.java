module io.avaje.inject.prism {
  
  exports io.avaje.inject.prism to io.avaje.inject.generator;
  
  requires static io.avaje.inject;
  
  requires static hickory;
  
  requires java.compiler;
  
}