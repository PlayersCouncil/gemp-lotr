package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.common.Result;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;

import java.util.List;

public interface PreGameDeckValidation {
    Result validatePreGameDeckCheck(List<PhysicalCardImpl> freeps, List<PhysicalCardImpl> shadow,
            List<PhysicalCardImpl> sites, PhysicalCardImpl rb, PhysicalCardImpl ring, PhysicalCardImpl map);
}
