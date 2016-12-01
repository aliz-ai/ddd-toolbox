package com.doctusoft.ddd.hibernate.persistence;

import com.doctusoft.ddd.jpa.persistence.JpaPersistence;
import com.doctusoft.ddd.model.Entity;
import com.doctusoft.hibernate.extras.HibernateMultiLineInsert;
import com.google.common.collect.Iterables;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        HibernateMultiLineInsert multiLineInsertSupport = getMultiLineInsertSupport(first);
        if (multiLineInsertSupport == null) {
            newEntities.forEach(this::insert);
        } else {
            Session session = em.unwrap(Session.class);
            Iterables
                .partition(newEntities, getMultiLineInsertLimit(kind))
                .forEach(partition -> multiLineInsertSupport.insertInBatch(session, partition.toArray()));
        }
    }
    
    protected int getMultiLineInsertLimit(Class<? extends Entity> kind) { return 1000; }
    
    @Nullable private HibernateMultiLineInsert getMultiLineInsertSupport(Entity entity) {
        SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        StatelessSessionImpl session = (StatelessSessionImpl) sessionFactory.openStatelessSession();
        EntityPersister persister = session.getEntityPersister(null, entity);
        return HibernateMultiLineInsert.lookup(persister);
    }
    
}