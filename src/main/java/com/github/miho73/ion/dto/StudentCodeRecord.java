package com.github.miho73.ion.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Entity
@Data
@NoArgsConstructor
@Table(schema = "users", name = "scode_rec")
public class StudentCodeRecord {
    @Id
    private int uuid;

    @Length(max = 15, message = "{validation.record.too_long}")
    @Column(name = "record", nullable = false)
    private String record;
}
