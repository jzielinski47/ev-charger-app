package jz.pk.evcm.dto.req.local;

public record ChargerSearchOptions(ChargerFilter filter, ChargerSort sort, Integer limit, Integer offset) {

    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT = 500;

    public static final ChargerSearchOptions DEFAULTS = new ChargerSearchOptions(null, null, null, null);

    public ChargerFilter effectiveFilter() {
        return filter != null ? filter : ChargerFilter.NONE;
    }

    public ChargerSort effectiveSort() {
        return sort != null ? sort : ChargerSort.DISTANCE;
    }

    public int effectiveLimit() {
        return limit != null ? limit : DEFAULT_LIMIT;
    }

    public int effectiveOffset() {
        return offset != null ? offset : 0;
    }
}
