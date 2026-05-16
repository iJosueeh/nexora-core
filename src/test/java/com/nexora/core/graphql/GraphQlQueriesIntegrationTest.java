package com.nexora.core.graphql;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.nexora.core.content.entity.Comment;
import com.nexora.core.content.entity.Post;
import com.nexora.core.content.repository.CommentRepository;
import com.nexora.core.content.repository.PostRepository;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.user.entity.Roles;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.RoleRepository;
import com.nexora.core.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class GraphQlQueriesIntegrationTest {

    private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfilesRepository profilesRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Roles studentRole;

    @BeforeEach
    void setupRole() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

                commentRepository.deleteAll();
                postRepository.deleteAll();
                profilesRepository.deleteAll();
                userRepository.deleteAll();

        studentRole = roleRepository.findByName(Role.ROLE_STUDENT.name()).orElseGet(() -> {
            Roles role = new Roles();
            role.setName(Role.ROLE_STUDENT.name());
            return roleRepository.save(role);
        });
    }

    @Test
    void obtenerFeedPrincipalShouldReturnPostsOrderedWithAuthorAndOfficialFlag() throws Exception {
        User author = buildUser("feed-" + UUID.randomUUID() + "@utp.edu.pe");
        Profiles profile = new Profiles();
        profile.setUser(author);
        profile.setUsername("feeduser_" + UUID.randomUUID().toString().substring(0, 8));
        profile.setFullName("Feed User");
        profile.setBio("bio");
        profile.setFollowersCount(0);
        profilesRepository.save(profile);

        Post oldPost = new Post();
        oldPost.setAutor(author);
        oldPost.setTitulo("Post viejo");
        oldPost.setContent("Contenido viejo");
        oldPost.setIsOfficial(false);
        oldPost.setStatus("PUBLISHED");
        oldPost = postRepository.save(oldPost);

        Post newPost = new Post();
        newPost.setAutor(author);
        newPost.setTitulo("Post nuevo");
        newPost.setContent("Contenido nuevo");
        newPost.setIsOfficial(true);
        newPost.setStatus("PUBLISHED");
        newPost = postRepository.save(newPost);

        jdbcTemplate.update("UPDATE posts SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 10, 0, 0)), oldPost.getId());
        jdbcTemplate.update("UPDATE posts SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 10, 5, 0)), newPost.getId());

        String query = """
                query {
                  obtenerFeedPrincipal(limit: 10, offset: 0) {
                    titulo
                    contenido
                    isOfficial
                    autor {
                      username
                      fullName
                    }
                  }
                }
                """;

        String body = graphQlBody(query);

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.obtenerFeedPrincipal[0].titulo").value("Post nuevo"))
                .andExpect(jsonPath("$.data.obtenerFeedPrincipal[0].isOfficial").value(true))
                .andExpect(jsonPath("$.data.obtenerFeedPrincipal[0].autor.username").value(profile.getUsername()))
                .andExpect(jsonPath("$.data.obtenerFeedPrincipal[1].titulo").value("Post viejo"));
    }

    @Test
    void comentariosPorPostShouldBuildNestedThread() throws Exception {
        User author = buildUser("thread-" + UUID.randomUUID() + "@utp.edu.pe");

        Post post = new Post();
        post.setAutor(author);
        post.setTitulo("Post para comentarios");
        post.setContent("Contenido base");
        post.setIsOfficial(false);
        post.setStatus("PUBLISHED");
        post = postRepository.save(post);

        Comment root = new Comment();
        root.setPost(post);
        root.setAutor(author);
        root.setContent("Comentario raiz");
        root = commentRepository.save(root);

        Comment reply = new Comment();
        reply.setPost(post);
        reply.setAutor(author);
        reply.setParent(root);
        reply.setContent("Respuesta hija");
        reply = commentRepository.save(reply);

        jdbcTemplate.update("UPDATE comentarios SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 2, 11, 0, 0)), root.getId());
        jdbcTemplate.update("UPDATE comentarios SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 2, 11, 1, 0)), reply.getId());

        String query = """
                query {
                  comentariosPorPost(postId: "%s") {
                    contenido
                    respuestas {
                      contenido
                    }
                  }
                }
                """.formatted(post.getId());

        String body = graphQlBody(query);

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.comentariosPorPost[0].contenido").value("Comentario raiz"))
                .andExpect(jsonPath("$.data.comentariosPorPost[0].respuestas[0].contenido").value("Respuesta hija"));
    }

    @Test
    void trendingTopicsShouldReturnPostsSortedByInteractionScoreIn24h() throws Exception {
        User author = buildUser("trending-" + UUID.randomUUID() + "@utp.edu.pe");
        Profiles profile = new Profiles();
        profile.setUser(author);
        profile.setUsername("trendinguser_" + UUID.randomUUID().toString().substring(0, 8));
        profile.setFullName("Trending User");
        profile.setBio("bio");
        profile.setFollowersCount(0);
        profilesRepository.save(profile);

        // Post 1: 5 interacciones (3 likes + 2 comentarios) en últimas 24h
        Post post1 = new Post();
        post1.setAutor(author);
        post1.setTitulo("Post Trending 1");
        post1.setContent("Contenido con trending");
        post1.setIsOfficial(false);
        post1.setStatus("PUBLISHED");
        post1 = postRepository.save(post1);

        // Post 2: 2 interacciones (2 likes) en últimas 24h
        Post post2 = new Post();
        post2.setAutor(author);
        post2.setTitulo("Post Trending 2");
        post2.setContent("Contenido con menos trending");
        post2.setIsOfficial(false);
        post2.setStatus("PUBLISHED");
        post2 = postRepository.save(post2);

        // Post 3: 1 interacción pero fuera de 24h
        Post post3 = new Post();
        post3.setAutor(author);
        post3.setTitulo("Post Viejo");
        post3.setContent("Viejo post");
        post3.setIsOfficial(false);
        post3.setStatus("PUBLISHED");
        post3 = postRepository.save(post3);

        // Agregar comentarios para post1 en últimas 24h
        Comment comment1 = new Comment();
        comment1.setPost(post1);
        comment1.setAutor(author);
        comment1.setContent("Comentario 1");
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setPost(post1);
        comment2.setAutor(author);
        comment2.setContent("Comentario 2");
        commentRepository.save(comment2);

        // Crear usuarios diferentes para los likes
        User liker1 = buildUser("liker1-" + UUID.randomUUID() + "@utp.edu.pe");
        User liker2 = buildUser("liker2-" + UUID.randomUUID() + "@utp.edu.pe");
        User liker3 = buildUser("liker3-" + UUID.randomUUID() + "@utp.edu.pe");
        User liker4 = buildUser("liker4-" + UUID.randomUUID() + "@utp.edu.pe");
        User liker5 = buildUser("liker5-" + UUID.randomUUID() + "@utp.edu.pe");
        User likerOld = buildUser("liker-old-" + UUID.randomUUID() + "@utp.edu.pe");

        // Agregar likes para post1 (3 likes en últimas 24h)
        jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, ?)",
                post1.getId(), liker1.getId(), Timestamp.valueOf(LocalDateTime.now()));
        jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, ?)",
                post1.getId(), liker2.getId(), Timestamp.valueOf(LocalDateTime.now()));
        jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, ?)",
                post1.getId(), liker3.getId(), Timestamp.valueOf(LocalDateTime.now()));

        // Agregar likes para post2 (2 likes en últimas 24h)
        jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, ?)",
                post2.getId(), liker4.getId(), Timestamp.valueOf(LocalDateTime.now()));
        jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, ?)",
                post2.getId(), liker5.getId(), Timestamp.valueOf(LocalDateTime.now()));

        // Agregar like para post3 pero 25 horas atrás (fuera de 24h)
        jdbcTemplate.update("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (?, ?, ?)",
                post3.getId(), likerOld.getId(), 
                Timestamp.valueOf(LocalDateTime.now().minusHours(25)));

        // Ajustar created_at: hacer post1 más reciente (now-1h) y post2 menos reciente (now-2h)
        jdbcTemplate.update("UPDATE posts SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1)), post1.getId());
        jdbcTemplate.update("UPDATE posts SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(2)), post2.getId());
        jdbcTemplate.update("UPDATE posts SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now().minusHours(30)), post3.getId());

        String query = """
                query {
                  trendingTopics(limit: 10) {
                    titulo
                    contenido
                    interactionScore
                    commentsCount
                    likesCount
                    autor {
                      username
                    }
                  }
                }
                """;

        String body = graphQlBody(query);

        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.trendingTopics[0].titulo").value("Post Trending 1"))
                .andExpect(jsonPath("$.data.trendingTopics[0].interactionScore").value(5))
                .andExpect(jsonPath("$.data.trendingTopics[0].commentsCount").value(2))
                .andExpect(jsonPath("$.data.trendingTopics[0].likesCount").value(3))
                .andExpect(jsonPath("$.data.trendingTopics[1].titulo").value("Post Trending 2"))
                .andExpect(jsonPath("$.data.trendingTopics[1].interactionScore").value(2))
                .andExpect(jsonPath("$.data.trendingTopics.length()").value(2));
    }

    private User buildUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setIsActive(true);
        user.setRole(studentRole);
        return userRepository.save(user);
    }

        private String graphQlBody(String query) {
                String escapedQuery = query
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\r", "")
                                .replace("\n", "\\n");
                return "{\"query\":\"" + escapedQuery + "\"}";
        }
}
