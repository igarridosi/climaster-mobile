package com.example.climaster.data.repository;

import com.climaster.data.remote.GroqApi;
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
public final class AgentRepositoryImpl_Factory implements Factory<AgentRepositoryImpl> {
  private final Provider<GroqApi> groqApiProvider;

  public AgentRepositoryImpl_Factory(Provider<GroqApi> groqApiProvider) {
    this.groqApiProvider = groqApiProvider;
  }

  @Override
  public AgentRepositoryImpl get() {
    return newInstance(groqApiProvider.get());
  }

  public static AgentRepositoryImpl_Factory create(Provider<GroqApi> groqApiProvider) {
    return new AgentRepositoryImpl_Factory(groqApiProvider);
  }

  public static AgentRepositoryImpl newInstance(GroqApi groqApi) {
    return new AgentRepositoryImpl(groqApi);
  }
}
