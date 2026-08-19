package com.appointment.handler.staff.entity;

import com.appointment.handler.auth.entity.User;
import com.appointment.handler.branch.entity.Branch;
import com.appointment.handler.business.entity.Business;
import com.appointment.handler.common.enums.StaffStatus;
import com.appointment.handler.service.entity.ServiceEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Entity
@Table(name = "staff")
@Document(collection = "staff")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @DocumentReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @DocumentReference
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    @DocumentReference
    private Branch branch;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StaffStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "staff_services",
            joinColumns = @JoinColumn(name = "staff_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    @DocumentReference
    @Builder.Default
    private Set<ServiceEntity> services = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = StaffStatus.ACTIVE;
        }
    }
}
