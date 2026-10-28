package com.zosh.repository;

import com.zosh.model.Asset;
import org.hibernate.property.access.internal.AbstractSetterMethodSerialForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset ,Long> {
    List<Asset> findByUserId(Long userId);

    Asset findByUserIdAndCoinId(Long userId , String coinId);
}
