package de.adito.jloadr.api;

import javax.annotation.*;

/**
 * An ILoader is responsible for loading a resource pack to the local store. After loading has completed a copy of the
 * source can be fetched from the store with the source's id.
 *
 * @author j.boesl, 05.09.16
 */
public interface ILoader
{

  @Nonnull
  IStoreResourcePack load(@Nonnull IStore pStore, @Nonnull IResourcePack pSource, @Nullable IStateCallback pStateCallback);


  interface IStateCallback
  {
    void inited(@Nullable IResource pSplashResource, int pElementCount);

    void loaded(int pElementNumber);

    void finished();
  }

}
