package su.plo.voice.api.sources;

import su.plo.voice.api.Pos3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface StaticAudioSource extends AudioSource {
    /**
     * ru_RU
     * Установить позицию источника
     */
    StaticAudioSource setPosition(@Nonnull String worldName, @Nonnull Pos3d position);

    /**
     * ru_RU
     * Получить названия мира источника
     */
    @Nonnull
    String getWorldName();

    /**
     * ru_RU
     * Получить позицию
     */
    @Nonnull
    Pos3d getPosition();

    /**
     * ru_RU
     * Установить угол направленности источнику
     * По умолчанию 360 (всенаправленный)
     *
     * @param direction Вектор направления источника
     * @param angle Угол направленности
     */
    StaticAudioSource setAngle(@Nonnull Pos3d direction, int angle);

    /**
     * ru_RU
     * Получить угол направленности источника
     */
    int getAngle();

    /**
     * ru_RU
     * Установить вектор направления источника
     */
    StaticAudioSource setDirection(@Nullable Pos3d direction);

    /**
     * ru_RU
     * Получить вектор направления источник
     */
    @Nullable
    Pos3d getDirection();
}
