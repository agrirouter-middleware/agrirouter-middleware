package de.agrirouter.middleware.persistence.search.rest;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.QApplication;
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
public interface SearchApplicationRepository extends JpaRepository<Application, Long>, QuerydslPredicateExecutor<Application>, QuerydslBinderCustomizer<QApplication> {

    @Override
    @SuppressWarnings("NullableProblems")
    default void customize(QuerydslBindings querydslBindings, QApplication qApplication) {
        querydslBindings.bind(String.class).first(
                (SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}
