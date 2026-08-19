package com.appointment.handler.business.repository;

import com.appointment.handler.business.entity.Business;
import com.appointment.handler.common.enums.BusinessStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mongodb")
public interface MongoBusinessRepository extends MongoRepository<Business, Long>, BusinessRepository {

    @Override
    @Query("{ '$and': [ " +
           "  { '$or': [ { $expr: { $eq: [?0, null] } }, { 'businessType.name': { '$regex': ?0, '$options': 'i' } } ] }, " +
           "  { '$or': [ { $expr: { $eq: [?1, null] } }, { 'branches.city': { '$regex': ?1, '$options': 'i' } } ] }, " +
           "  { '$or': [ { $expr: { $eq: [?2, null] } }, { 'status': ?2 } ] }, " +
           "  { '$or': [ { $expr: { $eq: [?3, null] } }, { 'name': { '$regex': ?3, '$options': 'i' } }, { 'description': { '$regex': ?3, '$options': 'i' } } ] } " +
           "] }")
    Page<Business> findAllFiltered(
            String typeName,
            String city,
            BusinessStatus status,
            String search,
            Pageable pageable
    );
}
