package index.alchemy.api;

public interface Time {
    
    public static final int HOUR = 1000, DAY = 24 * HOUR, WEEK = 7 * DAY, AVERAGE_YEAR_FEBRUARY = 28 * DAY,
            LEAP_YEAR_FEBRUARY = 29 * DAY, LUNAR_MONTH = 30 * DAY, SOLAR_MONTH = 31 * DAY,
            AVERAGE_YEAR = 365 * DAY, LEAP_YEAR = 366 * DAY;
    
}
