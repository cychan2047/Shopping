package com.chuwa.orderservice.repository;

import com.chuwa.orderservice.model.Order;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends CassandraRepository<Order, UUID> {

    /**
     * Finds all orders placed by a specific user.
     * Spring Data Cassandra automatically creates the implementation for this
     * method based on its name.
     * @param userId The ID of the user.
     * @return A list of orders belonging to the user.
     */
    Slice<Order> findByUserId(Long userId, Pageable pageable);
}