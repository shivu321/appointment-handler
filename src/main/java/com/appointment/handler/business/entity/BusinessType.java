package com.appointment.handler.business.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;

@Entity
@Table(name = "business_types")
@Document(collection = "business_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
