package org.yechan.remittance;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BaseEntityIntegrationTest {

  private static void setId(BaseEntity entity, Long id) {
    try {
      Field field = BaseEntity.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(entity, id);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Test
  void equalsReturnsTrueForSameId() {
    TestEntity first = new TestEntity();
    TestEntity second = new TestEntity();
    setId(first, 1L);
    setId(second, 1L);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equalsReturnsFalseForDifferentIds() {
    TestEntity first = new TestEntity();
    TestEntity second = new TestEntity();
    setId(first, 1L);
    setId(second, 2L);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equalsReturnsFalseWhenIdIsNull() {
    TestEntity first = new TestEntity();
    TestEntity second = new TestEntity();
    setId(second, 1L);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCodeMatchesForSameId() {
    TestEntity first = new TestEntity();
    TestEntity second = new TestEntity();
    setId(first, 1L);
    setId(second, 1L);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  // hibernate proxy test

  @Test
  void equalsReturnsTrueWhenComparingHibernateProxy() {
    TestEntity entity = new TestEntity();
    setId(entity, 1L);
    LazyInitializer initializer = Mockito.mock(LazyInitializer.class);
    Mockito.when(initializer.getPersistentClass()).thenAnswer(invocation -> TestEntity.class);
    TestEntityProxy proxy = new TestEntityProxy(initializer);
    setId(proxy, 1L);

    assertThat(entity).isEqualTo(proxy);
    assertThat(proxy).isEqualTo(entity);
  }

  private static class TestEntity extends BaseEntity {

  }

  private static class TestEntityProxy extends TestEntity implements HibernateProxy {

    private final LazyInitializer initializer;

    private TestEntityProxy(LazyInitializer initializer) {
      this.initializer = initializer;
    }

    @Override
    public LazyInitializer getHibernateLazyInitializer() {
      return initializer;
    }

    @Override
    public Object writeReplace() {
      return this;
    }
  }
}
