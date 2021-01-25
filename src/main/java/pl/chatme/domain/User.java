package pl.chatme.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * A User.
 */
@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = 3587541656966293052L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 60)
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 254, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean activated = false;

    @Column(name = "activation_key", length = 124)
    private String activationKey;

    @Column(name = "friend_request_code", nullable = false, length = 64, unique = true)
    private String friendRequestCode;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_authority",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "name")}
    )
    private Set<Authority> authorities;

    // To lowercase the email before saving it to db.
    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", activated=" + activated +
                ", authorities=" + authorities +
                '}';
    }
}
