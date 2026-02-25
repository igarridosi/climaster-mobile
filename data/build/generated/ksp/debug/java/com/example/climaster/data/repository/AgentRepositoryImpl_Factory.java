package com.example.climaster.data.repository;

import com.example.climaster.data.remote.GroqApi;
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

  private final Provider<String> apiKeyProvider;

  public AgentRepositoryImpl_Factory(Provider<GroqApi> groqApiProvider,
      Provider<String> apiKeyProvider) {
    this.groqApiProvider = groqApiProvider;
    this.apiKeyProvider = apiKeyProvider;
  }

  @Override
  public AgentRepositoryImpl get() {
    return newInstance(groqApiProvider.get(), apiKeyProvider.get());
  }

  public static AgentRepositoryImpl_Factory create(Provider<GroqApi> groqApiProvider,
      Provider<String> apiKeyProvider) {
    return new AgentRepositoryImpl_Factory(groqApiProvider, apiKeyProvider);
  }

  public static AgentRepositoryImpl newInstance(GroqApi groqApi, String apiKey) {
    return new AgentRepositoryImpl(groqApi, apiKey);
  }
}
