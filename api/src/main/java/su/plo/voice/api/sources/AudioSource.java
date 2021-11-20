package su.plo.voice.api.sources;


import su.plo.voice.api.player.VoicePlayer;

import javax.annotation.Nullable;
import java.util.List;

public interface AudioSource {
    /**
     * ru_RU
     * Айди источника
     */
    int getId();

    /**
     * ru_RU
     * Установить handler звука
     */
    void setSourceHandler(AudioSourceHandler handler);

    /**
     * ru_RU
     * член
     */
    void close();

    /**
     * ru_RU
     * Отправить PCM аудио от источника
     * @param distance Дистанция на которой источник будет слышно
     * @param samples signed 16-bit little-endian samples
     */
    void send(short[] samples, int distance);

    /**
     * ru_RU
     * Отправить PCM аудио от источника
     * @param distance Дистанция на которой источник будет слышно
     * @param samples signed 16-bit little-endian samples
     * @param encoded set true if data is already encoded
     */
    void send(byte[] samples, boolean encoded, int distance, Long timestamp, long sequenceNumber);

    /**
     * ru_RU
     * Отправить PCM аудио от источника
     * @param distance Дистанция на которой источник будет слышно
     * @param samples unencoded signed 16-bit little-endian samples
     */
    void send(short[] samples, int distance, Long timestamp, long sequenceNumber);

    /**
     * ru_RU
     * Отправить "end" текущего потока
     */
    void sendEnd(int distance, long sequenceNumber);

    /**
     * ru_RU
     * Отправить "end" текущего потока
     */
    void sendEnd(int distance);

    /**
     * ru_RU
     * Видно ли иконку источника
     */
    boolean isVisible();

    /**
     * ru_RU
     * Установить видимость иконки у истоника
     */
    void setVisible(boolean visible);

    /**
     * ru_RU
     * Установить режим энкодера
     * По умолчанию VOIP
     */
    void setApplication(Application mode);

    /**
     * ru_RU
     * Получить режим энкодера
     */
    @Nullable
    Application getApplication();

    /**
     * ru_RU
     * Установить битрейт энкодеру
     */
    void setBitrate(int bitrate);

    /**
     * ru_RU
     * Получить битрейт энкодера
     */
    int getBitrate();

    /**
     * ru_RU
     * Encode audio with opus
     */
    byte[] encode(short[] samples);

    /**
     * ru_RU
     * Получить список "слушателей"
     *
     * @param distance Радиус
     */
    List<VoicePlayer> getListeners(short distance);

    enum Application {
        VOIP,
        AUDIO,
        RESTRICTED_LOWDELAY;

        public int value() {
            return switch (this) {
                case VOIP -> 2048;
                case AUDIO -> 2049;
                case RESTRICTED_LOWDELAY -> 2051;
            };
        }

        @Nullable
        public static Application of(int mode) {
            return switch (mode) {
                case 2048 -> VOIP;
                case 2049 -> AUDIO;
                case 2051 -> RESTRICTED_LOWDELAY;
                default -> null;
            };
        }
    }
}
