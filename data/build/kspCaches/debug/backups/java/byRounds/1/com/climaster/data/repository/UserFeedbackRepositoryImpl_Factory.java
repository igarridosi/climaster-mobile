package com.climaster.data.repository;

import com.climaster.data.local.UserFeedbackDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class UserFeedbackRepositoryImpl_Factory implements Factory<UserFeedbackRepositoryImpl> {
  private final Provider<UserFeedbackDao> daoProvider;

  public UserFeedbackRepositoryImpl_Factory(Provider<UserFeedbackDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public UserFeedbackRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static UserFeedbackRepositoryImpl_Factory create(Provider<UserFeedbackDao> daoProvider) {
    return new UserFeedbackRepositoryImpl_Factory(daoProvider);
  }

  public static UserFeedbackRepositoryImpl newInstance(UserFeedbackDao dao) {
    return new UserFeedbackRepositoryImpl(dao);
  }
}
