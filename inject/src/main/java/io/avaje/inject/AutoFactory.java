package io.avaje.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register an interface (or abstract class) as a bean where the implementation is created at
 * runtime by a {@link Creator} bean.
 *
 * <p>This is useful for libraries that create implementations of interfaces dynamically, where
 * there is no concrete class for avaje-inject to construct.
 *
 * <p>For each interface annotated with {@code @AutoFactory} a {@code @Factory} class is generated
 * with a {@code @Bean} method that obtains the implementation from the specified {@link Creator}.
 * The {@link Creator} implementation must itself be a bean (for example annotated with
 * {@code @Singleton}) and is injected into the generated factory.
 *
 * <p>Qualifier annotations (like {@code @Named}) along with {@code @Primary}, {@code @Secondary},
 * {@code @Prototype} and {@code @Lazy} on the annotated type are applied to the generated
 * {@code @Bean} method.
 *
 * <h3>Example</h3>
 *
 * <pre>{@code
 * @Singleton
 * final class MapperCreator implements AutoFactory.Creator {
 *
 *   private final SqlSession sqlSession;
 *
 *   MapperCreator(SqlSession sqlSession) {
 *     this.sqlSession = sqlSession;
 *   }
 *
 *   @Override
 *   public <T> T create(Class<T> type) {
 *     return sqlSession.getMapper(type);
 *   }
 * }
 *
 * @AutoFactory(MapperCreator.class)
 * public interface UserMapper {
 *
 *   @Select("SELECT * FROM users WHERE id = #{id}")
 *   User findById(long id);
 * }
 *
 * }</pre>
 *
 * <p>With the above, {@code UserMapper} can be injected like any other bean.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoFactory {

  /** The {@link Creator} bean type used to create the implementation of the annotated type. */
  Class<? extends Creator> value();

  /**
   * Creates implementations for types annotated with {@link AutoFactory}.
   *
   * <p>Implementations must be beans so they can be injected into the generated factories.
   */
  @FunctionalInterface
  interface Creator {

    /**
     * Create an implementation of the given type.
     *
     * @param type the type annotated with {@link AutoFactory}
     * @return an instance implementing the given type
     */
    <T> T create(Class<T> type);
  }

  /**
   * Register types that can't be annotated with {@code @AutoFactory} directly, typically because
   * they are in an external project / dependency.
   *
   * <p>The factories are generated in the package of the annotated element.
   *
   * <pre>{@code
   * @AutoFactory.Import(value = {UserMapper.class, OrderMapper.class}, creator = MapperCreator.class)
   * package org.example.mappers;
   *
   * }</pre>
   */
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
  @Repeatable(Imports.class)
  @interface Import {

    /** The interfaces or abstract classes to register as beans. */
    Class<?>[] value();

    /** The {@link Creator} bean type used to create the implementations. */
    Class<? extends Creator> creator();
  }

  /**
   * @see Import
   */
  @Retention(RetentionPolicy.SOURCE)
  @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
  @interface Imports {
    Import[] value();
  }
}
