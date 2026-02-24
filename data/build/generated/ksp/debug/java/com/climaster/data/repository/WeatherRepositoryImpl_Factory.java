package com.climaster.data.repository;

import com.climaster.data.local.WeatherDao;
import com.climaster.data.remote.WeatherApi;
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
public final class WeatherRepositoryImpl_Factory implements Factory<WeatherRepositoryImpl> {
  private final Provider<WeatherApi> apiProvider;

  private final Provider<WeatherDao> daoProvider;

  public WeatherRepositoryImpl_Factory(Provider<WeatherApi> apiProvider,
      Provider<WeatherDao> daoProvider) {
    this.apiProvider = apiProvider;
    this.daoProvider = daoProvider;
  }

  @Override
  public WeatherRepositoryImpl get() {
    return newInstance(apiProvider.get(), daoProvider.get());
  }

  public static WeatherRepositoryImpl_Factory create(Provider<WeatherApi> apiProvider,
      Provider<WeatherDao> daoProvider) {
    return new WeatherRepositoryImpl_Factory(apiProvider, daoProvider);
  }

  public static WeatherRepositoryImpl newInstance(WeatherApi api, WeatherDao dao) {
    return new WeatherRepositoryImpl(api, dao);
  }
}
