package com.gempukku.lotro.filters;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

public interface Filter extends Filterable {
    boolean accepts(LotroGame game, PhysicalCard physicalCard);
}
