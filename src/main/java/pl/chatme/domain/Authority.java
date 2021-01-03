package pl.chatme.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * An authority (a security role) used by Spring Security.
 */
@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Authority implements Serializable {

    public static final long serialVersionUID = -8053205789790776096L;

    @Id
    @Column(length = 50, nullable = false, unique = true)
    @NotBlank
    @Size(max = 50)
    private String name;

    // To uppercase the authority name before saving it to db.
    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    @Override
    public String toString() {
        return "Authority{" +
                "name='" + name + '\'' +
                '}';
    }
}
