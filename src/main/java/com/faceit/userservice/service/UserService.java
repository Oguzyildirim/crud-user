package com.faceit.userservice.service;

import com.faceit.userservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link User}.
 */
public interface UserService {

    /**
     * Save a user.
     *
     * @param faceIt the entity to save.
     * @return the persisted entity.
     */
    User save(User faceIt);

    /**
     * Get all the users.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Get all the users by country.
     *
     * @param country the user country code.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<User> findAllByCountry(String country, Pageable pageable);


    /**
     * Get the "id" user.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<User> findOne(Long id);

    /**
     * Delete the "id" user.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
