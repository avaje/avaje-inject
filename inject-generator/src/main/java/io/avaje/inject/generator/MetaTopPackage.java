package io.avaje.inject.generator;

import java.util.Collection;

final class MetaTopPackage {

  private String topPackage;

  static String of(Collection<MetaData> values) {
    return new MetaTopPackage(values).value();
  }

  private String value() {
    return topPackage;
  }

  private MetaTopPackage(Collection<MetaData> values) {
    for (MetaData metaData : values) {
      topPackage = Util.commonParent(topPackage, metaData.topPackage());
    }
  }
}
