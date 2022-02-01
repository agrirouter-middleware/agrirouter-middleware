package de.agrirouter.middleware.persistence.search.rest;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.QEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.stereotype.Repository;

/**
 * Repository to access all content messages.
 */
@Repository
public interface SearchEndpointRepository extends JpaRepository<Endpoint, Long>, QuerydslPredicateExecutor<Endpoint>, QuerydslBinderCustomizer<QEndpoint> {

    @Override
    @SuppressWarnings("NullableProblems")
    default void customize(QuerydslBindings querydslBindings, QEndpoint qEndpoint) {
        querydslBindings.bind(String.class).first(
                (SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}
