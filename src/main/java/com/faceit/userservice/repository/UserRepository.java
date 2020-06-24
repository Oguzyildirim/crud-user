package com.faceit.userservice.repository;


import com.faceit.userservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the User entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Lookup a Page of Users associated with a Country
     *
     * @param country the user country code.
     * @param pageable details for finding the correct page.
     * @return A page of users if found, empty otherwise.
     */
    Page<User> findAllByCountry(String country, Pageable pageable);
}
