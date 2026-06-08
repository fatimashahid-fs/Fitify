package com.fitify.interfaces;

import java.util.List;
import java.util.Optional;

/**
 * Storable<T> - Generic DAO interface defining CRUD for any entity T.
 * OOP Concept: ABSTRACTION + GENERICS
 * Implemented by: UserDAO, WorkoutDAO, TrackDAO
 */
public interface Storable<T> {
    boolean     save(T entity);
    Optional<T> findById(int id);
    List<T>     findAll();
    boolean     update(T entity);
    boolean     delete(int id);
}
