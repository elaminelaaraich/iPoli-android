package io.ipoli.android.pet.data;

import android.support.annotation.ColorRes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.persistence.PersistedObject;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/16.
 */
@IgnoreExtraProperties
public class Pet extends PersistedObject {

    private String name;
    private Integer healthPointsPercentage;
    private Integer experienceBonusPercentage;
    private Integer coinsBonusPercentage;
    private String picture;
    private String backgroundPicture;

    public Pet() {

    }

    public Pet(String name, String picture, String backgroundPicture, Integer healthPointsPercentage) {
        this.name = name;
        this.picture = picture;
        this.backgroundPicture = backgroundPicture;
        setHealthPointsPercentage(healthPointsPercentage);
        setCreatedAt(DateUtils.nowUTC().getTime());
        setUpdatedAt(DateUtils.nowUTC().getTime());
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getBackgroundPicture() {
        return backgroundPicture;
    }

    public void setBackgroundPicture(String backgroundPicture) {
        this.backgroundPicture = backgroundPicture;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHealthPointsPercentage() {
        return healthPointsPercentage;
    }

    public void setHealthPointsPercentage(Integer healthPointsPercentage) {
        this.healthPointsPercentage = Math.max(0, Math.min(100, healthPointsPercentage));
        updateExperienceBonusPercentage();
        updateCoinsBonusPercentage();
    }

    public Integer getExperienceBonusPercentage() {
        return experienceBonusPercentage;
    }

    public void setExperienceBonusPercentage(Integer experienceBonusPercentage) {
        this.experienceBonusPercentage = Math.max(0, Math.min(Constants.MAX_PET_XP_BONUS, experienceBonusPercentage));
    }

    public Integer getCoinsBonusPercentage() {
        return coinsBonusPercentage;
    }

    public void setCoinsBonusPercentage(Integer coinsBonusPercentage) {
        this.coinsBonusPercentage = Math.max(0, Math.min(Constants.MAX_PET_COIN_BONUS, coinsBonusPercentage));
    }

    public void addHealthPoints(int healthPoints) {
        setHealthPointsPercentage(getHealthPointsPercentage() + healthPoints);
    }

    private void updateCoinsBonusPercentage() {
        setCoinsBonusPercentage((int) Math.floor(getHealthPointsPercentage() * Constants.COINS_BONUS_PERCENTAGE_OF_HP / 100.0));
    }

    private void updateExperienceBonusPercentage() {
        setExperienceBonusPercentage((int) Math.floor(getHealthPointsPercentage() * Constants.XP_BONUS_PERCENTAGE_OF_HP / 100.0));
    }

    @Exclude
    public String getStateText() {
        return getState().name().toLowerCase();
    }

    @Exclude
    @ColorRes
    public int getStateColor() {
        return getState().color;
    }

    @Exclude
    public PetState getState() {
        if (healthPointsPercentage >= 90) {
            return PetState.AWESOME;
        }
        if (healthPointsPercentage >= 60) {
            return PetState.HAPPY;
        }
        if (healthPointsPercentage >= 35) {
            return PetState.GOOD;
        }
        if (healthPointsPercentage > 0) {
            return PetState.SAD;
        }
        return PetState.DEAD;
    }

    public enum PetState {
        AWESOME(R.color.md_green_500),
        HAPPY(R.color.md_orange_500),
        GOOD(R.color.md_yellow_500),
        SAD(R.color.md_red_500),
        DEAD(R.color.md_black);

        public final int color;

        PetState(@ColorRes int color) {
            this.color = color;
        }
    }
}
