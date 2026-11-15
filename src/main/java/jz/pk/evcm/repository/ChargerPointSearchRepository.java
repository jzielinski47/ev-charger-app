package jz.pk.evcm.repository;

import java.util.List;

public interface ChargerPointSearchRepository {

    List<ChargerSearchHit> searchInRadius(double latitude, double longitude, double distanceInKm, ChargerSearchCriteria criteria);

    List<ChargerSearchHit> searchInBounds(double north, double south, double east, double west,
                                          ChargerSearchCriteria criteria);
}
