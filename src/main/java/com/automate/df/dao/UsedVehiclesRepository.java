package com.automate.df.dao;

import com.automate.df.entity.DmsUsedVehicles;
import com.automate.df.entity.oh.LocationNodeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsedVehiclesRepository extends JpaRepository<DmsUsedVehicles, Long> {

    @Query(value = "SELECT * FROM dms_used_vehicles where org_id=:org_id and status=:status", nativeQuery = true)
    List<DmsUsedVehicles> usedVehiclesFindByOrgId(@Param(value = "org_id") Integer org_id, @Param(value = "status")  String status);

    @Query(value = "SELECT * FROM dms_used_vehicles where rc_number=:rc_number", nativeQuery = true)
    DmsUsedVehicles usedVehiclesByRCNo(@Param(value = "rc_number") String rc_number);

}
