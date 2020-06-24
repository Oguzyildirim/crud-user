package com.faceit.userservice.web.rest;

import com.faceit.userservice.UserServiceApplication;
import com.faceit.userservice.domain.User;
import com.faceit.userservice.repository.UserRepository;
import com.faceit.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for the {@link UserController} REST controller.
 */
@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    private static final String INITIAL_FIRST_NAME = "FACEITName";
    private static final String UPDATED_FIRST_NAME = "FACEITNameUPDATED";

    private static final String INITIAL_LAST_NAME = "FACEITLastName";
    private static final String UPDATED_LAST_NAME = "FACEITLastNameUPDATED";

    private static final String INITIAL_EMAIL = "FACEITMail";
    private static final String UPDATED_EMAIL = "FACEITMailUPDATED";

    private static final String INITIAL_NICKNAME = "FACEITNickname";
    private static final String UPDATED_NICKNAME = "FACEITNicknameUPDATED";

    private static final String INITIAL_PASSWORD = "FACEITPassword";
    private static final String UPDATED_PASSWORD = "FACEITPasswordUPDATED";

    private static final String INITIAL_COUNTRY = "FACEITCountry";
    private static final String UPDATED_COUNTRY = "FACEITCountryUpdated";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserMockMvc;

    private User user;

    /**
     * Create an entity for this test.
     */
    public static User createEntity(EntityManager em) {
        User user = new User()
            .firstName(INITIAL_FIRST_NAME)
            .lastName(INITIAL_LAST_NAME)
            .email(INITIAL_EMAIL)
            .nickname(INITIAL_NICKNAME)
            .password(INITIAL_PASSWORD)
            .country(INITIAL_COUNTRY);
        return user;
    }
    /**
     * Create an updated entity for this test.
     *
     */
    public static User createUpdatedEntity(EntityManager em) {
        User user = new User()
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .nickname(UPDATED_NICKNAME)
            .password(UPDATED_PASSWORD)
            .country(UPDATED_COUNTRY);
        return user;
    }

    @BeforeEach
    public void initTest() {
        user = createEntity(em);
    }

    @Test
    @Transactional
    public void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();
        // Create the User
        restUserMockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(user)))
            .andExpect(status().isCreated());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getFirstName()).isEqualTo(INITIAL_FIRST_NAME);
        assertThat(testUser.getLastName()).isEqualTo(INITIAL_LAST_NAME);
        assertThat(testUser.getEmail()).isEqualTo(INITIAL_EMAIL);
        assertThat(testUser.getNickname()).isEqualTo(INITIAL_NICKNAME);
        assertThat(testUser.getPassword()).isEqualTo(INITIAL_PASSWORD);
        assertThat(testUser.getCountry()).isEqualTo(INITIAL_COUNTRY);
    }

    @Test
    @Transactional
    public void createUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        // Create the User with an existing ID
        user.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(user)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the userList
        restUserMockMvc.perform(get("/api/users?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(user.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(INITIAL_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(INITIAL_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(INITIAL_EMAIL)))
            .andExpect(jsonPath("$.[*].nickname").value(hasItem(INITIAL_NICKNAME)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(INITIAL_PASSWORD)))
            .andExpect(jsonPath("$.[*].country").value(hasItem(INITIAL_COUNTRY)));
    }
    
    @Test
    @Transactional
    public void getUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get the user
        restUserMockMvc.perform(get("/api/users/{id}", user.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(user.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(INITIAL_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(INITIAL_LAST_NAME))
            .andExpect(jsonPath("$.email").value(INITIAL_EMAIL))
            .andExpect(jsonPath("$.nickname").value(INITIAL_NICKNAME))
            .andExpect(jsonPath("$.password").value(INITIAL_PASSWORD))
            .andExpect(jsonPath("$.country").value(INITIAL_COUNTRY));
    }
    @Test
    @Transactional
    public void getNonExistingUser() throws Exception {
        // Get the user
        restUserMockMvc.perform(get("/api/users/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUser() throws Exception {
        // Initialize the database
        userService.save(user);

        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).get();
        // Disconnect from session so that the updates on updatedUser are not directly saved in db
        em.detach(updatedUser);
        updatedUser
            .firstName(UPDATED_FIRST_NAME)
            .lastName(UPDATED_LAST_NAME)
            .email(UPDATED_EMAIL)
            .nickname(UPDATED_NICKNAME)
            .password(UPDATED_PASSWORD)
            .country(UPDATED_COUNTRY);

        restUserMockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedUser)))
            .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LAST_NAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUser.getNickname()).isEqualTo(UPDATED_NICKNAME);
        assertThat(testUser.getPassword()).isEqualTo(UPDATED_PASSWORD);
        assertThat(testUser.getCountry()).isEqualTo(UPDATED_COUNTRY);
    }

    @Test
    @Transactional
    public void updateNonExistingUser() throws Exception {
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserMockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(user)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteUser() throws Exception {
        // Initialize the database
        userService.save(user);

        int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the User
        restUserMockMvc.perform(delete("/api/users/{id}", user.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
