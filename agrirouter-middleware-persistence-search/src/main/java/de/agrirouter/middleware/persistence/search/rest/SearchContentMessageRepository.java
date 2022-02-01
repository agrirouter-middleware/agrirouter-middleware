package de.agrirouter.middleware.persistence.search.rest;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import de.agrirouter.middleware.domain.ContentMessage;
import de.agrirouter.middleware.domain.QContentMessage;
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
public interface SearchContentMessageRepository extends JpaRepository<ContentMessage, Long>, QuerydslPredicateExecutor<ContentMessage>, QuerydslBinderCustomizer<QContentMessage> {

    @Override
    @SuppressWarnings("NullableProblems")
    default void customize(QuerydslBindings querydslBindings, QContentMessage qContentMessage) {
        querydslBindings.bind(String.class).first(
                (SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}
