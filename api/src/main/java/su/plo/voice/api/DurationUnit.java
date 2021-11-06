package su.plo.voice.api;

public enum DurationUnit {
    SECONDS,
    MINUTES,
    HOURS,
    DAYS,
    WEEKS,
    TIMESTAMP;

    public long multiply(long duration) {
        switch (this) {
            case MINUTES:
                return duration * 60;
            case HOURS:
                return duration * 3600;
            case DAYS:
                return duration * 86400;
            case WEEKS:
                return duration * 604800;
            default:
                return duration;
        }
    }

    public String format(ConfigMessages messages, long duration) {
        switch (this) {
            case MINUTES:
                return String.format(messages.getMessage("mute_durations.minutes"), duration);
            case HOURS:
                return String.format(messages.getMessage("mute_durations.hours"), duration);
            case DAYS:
                return String.format(messages.getMessage("mute_durations.days"), duration);
            case WEEKS:
                return String.format(messages.getMessage("mute_durations.weeks"), duration);
            default:
                return String.format(messages.getMessage("mute_durations.seconds"), duration);
        }
    }
}
