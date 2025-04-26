package com.gempukku.lotro.game;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Zone;

public interface PhysicalCard extends Filterable {
    boolean isFlipped();
    void setFlipped(boolean flipped);
    Zone getZone();
    void setPlayedFromZone(Zone zone);
    Zone getPlayedFromZone();

    String getBlueprintId();

    String getOwner();

    String getCardController();

    int getCardId();

    LotroCardBlueprint getBlueprint();

    PhysicalCard getAttachedTo();

    PhysicalCard getStackedOn();

    void setWhileInZoneData(WhileInZoneData whileInZoneData);

    WhileInZoneData getWhileInZoneData();

    void setSiteNumber(Integer number);

    Integer getSiteNumber();

    interface WhileInZoneData {
        String getValue();

        String getHumanReadable();
    }
}
