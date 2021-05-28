package com.sds.iot.dto;

import java.io.Serializable;

/**
 * Value class for calculation
 */
public class AggregateValue implements Serializable {

    private Long count;
    private Long sum;

    public AggregateValue(Long count, Long sum) {
        super();
        this.count = count;
        this.sum = sum;
    }

    public Long getCount() {
        return count;
    }

    public Long getSum() {
        return sum;
    }
/*
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
        result = prime * result + ((vehicleType == null) ? 0 : vehicleType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AggregateValue) {
            AggregateValue other = (AggregateValue) obj;
            if (other.getRouteId() != null && other.getVehicleType() != null) {
                if ((other.getRouteId().equals(this.routeId)) && (other.getVehicleType().equals(this.vehicleType))) {
                    return true;
                }
            }
        }
        return false;
    }
*/

}
