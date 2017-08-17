package com.doctusoft.ddd.jpa.criteria;

import com.doctusoft.ddd.model.Entity;
import com.doctusoft.ddd.model.EntityClass;
import com.doctusoft.java.Failsafe;
import com.doctusoft.math.ClosedRange;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.criteria.*;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.function.*;

import static java.util.Objects.*;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class EntityCriteria<T extends Entity> {
    
    @NonNull private final EntityClass<? extends T> entityClass;
    
    @NonNull private final CriteriaBuilder builder;
    
    @Getter(AccessLevel.PACKAGE)
    private final List<Function<Root, Predicate>> conditions = new ArrayList<>();
    
    public EntityCriteria<T> addCondition(Function<Root, Predicate> condition) {
        conditions.add(condition);
        return this;
    }
    
    void applyConditions(Root<?> root, Consumer<Predicate[]> whereFun) {
        if (conditions.size() > 0) {
            whereFun.accept(conditions
                .stream()
                .map(f -> f.apply(root))
                .toArray(Predicate[]::new));
        }
    }
    
    <U> Function<Root, Path<U>> get(String attributeName) { return root -> root.get(attributeName); }
    
    public <U> EntityCriteria<T> filter(String attributeName, Function<Expression<U>, Predicate> filter) {
        Function<Root, Path<U>> attrFun = get(attributeName);
        addCondition(attrFun.andThen(filter));
        return this;
    }
    
    public EntityCriteria<T> filter(String attributeName1, BinaryFilterOperation operator, String attributeName2) {
        addCondition(r -> {
            Path path1 = r.get(attributeName1);
            Path path2 = r.get(attributeName2);
            return operator.apply(builder, path1, path2);
        });
        return this;
    }
    
    public EntityCriteria<T> equalTo(String attributeName, Object value) {
        requireNonNull(value);
        return filter(attributeName, attr -> builder.equal(attr, value));
    }
    
    public EntityCriteria<T> like(String attributeName, String pattern) {
        requireNonNull(pattern);
        return filter(attributeName, (Expression<String> attr) -> builder.like(attr, pattern));
    }
    
    public EntityCriteria<T> startsWith(String attributeName, String prefix) {
        return like(attributeName, prefix + "%");
    }
    
    Function<Root, ? extends Expression<String>> id() {
        if (entityClass.hasStringId()) return get(Entity.ID);
        return idToString();
    }
    
    Function<Root, Expression<String>> idToString() {
        return root -> builder.toString(root.get(Entity.ID));
    }
    
    public EntityCriteria<T> idEquals(Object value) {
        entityClass.checkId(value);
        return filterId(id -> builder.equal(id, value));
    }
    
    public EntityCriteria<T> idIn(Collection<?> ids) {
        ids.forEach(entityClass::checkId);
        return filterId(id -> id.in(ids));
    }
    
    public EntityCriteria<T> idInNumericRange(ClosedRange<Long> idRange) {
        Failsafe.checkState(entityClass.hasLongId(), "Not EntityWithLongId: " + entityClass);
        return addCondition(root -> builder.between(root.get(Entity.ID), idRange.getLowerBound(), idRange.getUpperBound()));
    }
    
    public EntityCriteria<T> idInLexicographicalRange(ClosedRange<String> idRange) {
        Failsafe.checkState(entityClass.hasStringId(), "Not EntityWithStringId: " + entityClass);
        return addCondition(root -> builder.between(root.get(Entity.ID), idRange.getLowerBound(), idRange.getUpperBound()));
    }
    
    public EntityCriteria<T> idLike(String idPattern) {
        return filterId(id -> builder.like(id, idPattern));
    }
    
    public EntityCriteria<T> idStartsWith(String idPrefix) { return idLike(idPrefix + "%"); }
    
    public EntityCriteria<T> compareId(BiFunction<Expression, Comparable, Predicate> operator, Comparable value) {
        entityClass.checkId(value);
        return filterId(id -> operator.apply(id, value));
    }
    
    public EntityCriteria<T> filterId(Function<Expression<String>, Predicate> idFilter) {
        return addCondition(id().andThen(idFilter));
    }
    
}
