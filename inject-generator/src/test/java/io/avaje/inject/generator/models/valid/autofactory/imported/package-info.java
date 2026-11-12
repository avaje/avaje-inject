@AutoFactory.Import(value = ExtMapper.class, creator = MyCreator.class)
@AutoFactory.Import(value = OtherExtMapper.class, creator = MyCreator.class)
package io.avaje.inject.generator.models.valid.autofactory.imported;

import io.avaje.inject.AutoFactory;
import io.avaje.inject.generator.models.valid.autofactory.MyCreator;
import io.avaje.inject.generator.models.valid.autofactory.ext.ExtMapper;
import io.avaje.inject.generator.models.valid.autofactory.ext.OtherExtMapper;
