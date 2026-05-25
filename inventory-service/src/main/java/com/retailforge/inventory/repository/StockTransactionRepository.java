package com.retailforge.inventory.repository;

import com.retailforge.inventory.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByProductId(Long productId);
    List<StockTransaction> findByWarehouseId(Long warehouseId);
}
