package de.adito.jloadr.api;

/**
 * An ILoader is responsible for loading a resource pack to the local store. After loading has completed a copy of the
 * source can be fetched from the store with the source's id.
 *
 * @author j.boesl, 05.09.16
 */
public interface ILoader
{

  IStoreResourcePack load(IStore pStore, IResourcePack pSource, IStateCallback pStateCallback);


  interface IStateCallback
  {
    void setSplashResource(IResource pSplashResource);

    void setProgress(int pAbsolute, double pRelative);

    void finished();
  }

}
