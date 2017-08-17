package com.doctusoft.ddd.hibernate.persistence;

import com.doctusoft.ddd.jpa.criteria.EntityQuery;
import com.doctusoft.ddd.jpa.persistence.JpaPersistence;
import com.doctusoft.ddd.model.Entity;
import com.doctusoft.hibernate.extras.HibernateMultiLineInsert;
import com.google.common.collect.Iterables;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.TypedQuery;
import java.util.*;
import java.util.function.*;

import static java.util.Objects.*;

public abstract class HibernatePersistence extends JpaPersistence {
    
    public <T extends Entity> void insertMany(@NotNull Class<T> kind, @NotNull Collection<? extends T> newEntities) {
        requireNonNull(kind, "kind");
        if (newEntities.isEmpty()) {
            return;
        }
        Consumer<Entity> preInsertAction = createPreInsertAction(kind);
        newEntities.forEach(preInsertAction);
        T first = newEntities.iterator().next();
        if (newEntities.size() == 1) {
            insert(first);
            return;
        }
        HibernateMultiLineInsert multiLineInsertSupport = getMultiLineInsertSupport(first);
        if (multiLineInsertSupport == null) {
            newEntities.forEach(this::insert);
            return;
        }
        int multiLineInsertLimit = getMultiLineInsertLimit(kind);
        if (multiLineInsertLimit < 10) {
            newEntities.forEach(this::insert);
            return;
        }
        em.flush();
        Session session = em.unwrap(Session.class);
        Iterables
            .partition(newEntities, multiLineInsertLimit)
            .forEach(partition -> multiLineInsertSupport.insertInBatch(session, partition.toArray()));
    }
    
    protected int getMultiLineInsertLimit(Class<? extends Entity> kind) { return 1000; }
    
    @Nullable private HibernateMultiLineInsert getMultiLineInsertSupport(Entity entity) {
        SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        StatelessSessionImpl session = (StatelessSessionImpl) sessionFactory.openStatelessSession();
        EntityPersister persister = session.getEntityPersister(null, entity);
        return HibernateMultiLineInsert.lookup(persister);
    }
    
    protected static <T> @NotNull org.hibernate.query.Query<T> unwrap(TypedQuery<T> typedQuery) {
        return (org.hibernate.query.Query<T>) typedQuery.unwrap(Query.class);
    }
    
    protected static <T extends Entity> @NotNull Query<T> unwrap(EntityQuery<T> query) {
        return unwrap(query.query());
    }
    
}
