package jz.pk.evcm.repository;

import java.util.List;

public interface ChargerPointSearchRepository {

    List<Long> searchInRadius(double latitude, double longitude, double distanceInKm, ChargerSearchCriteria criteria);
}
