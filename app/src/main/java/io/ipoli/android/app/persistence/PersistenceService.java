package io.ipoli.android.app.persistence;

import java.util.List;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.RealmObject;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/30/16.
 */
public interface PersistenceService<T extends RealmObject & RemoteObject> {

    Observable<T> save(T obj);

    Observable<List<T>> saveAll(List<T> objs);

    Observable<T> saveRemoteObject(T object);

    Observable<List<T>> saveRemoteObjects(List<T> objects);

    Observable<List<T>> findAllWhoNeedSyncWithRemote();

    void updateId(T obj, String newId);

    Observable<T> findById(String id);
}