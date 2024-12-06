package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.modifiers.ModifiersQuerying;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.NormalSkirmishResult;

import java.util.Set;

public class ResolveSkirmishDamageAction extends RequiredTriggerAction {
    private final NormalSkirmishResult _skirmishResult;

    private Integer _damageToDo;
    private int _remainingDamage;

    public ResolveSkirmishDamageAction(NormalSkirmishResult skirmishResult) {
        super(null);
        setText("Resolve skirmish damage");
        _skirmishResult = skirmishResult;
    }

    @Override
    public Type getType() {
        return Type.RESOLVE_DAMAGE;
    }

    @Override
    public Effect nextEffect(LotroGame game) {
        if (_damageToDo == null) {
            _damageToDo = calculateDamageToDo(game);
            _remainingDamage = _damageToDo;
        }

        if (_remainingDamage > 0) {
            _remainingDamage--;
            return new WoundCharactersEffect(_skirmishResult.getWinners(), Filters.in(_skirmishResult.getInSkirmishLosers()),
                    new Filter() {
                        @Override
                        public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                            return game.getModifiersQuerying().canTakeWoundsFromLosingSkirmish(game, physicalCard);
                        }
                    });
        }

        return null;
    }

    public void consumeRemainingDamage() {
        _remainingDamage = 0;
    }

    public int getRemainingDamage() {
        return _remainingDamage;
    }

    private int calculateDamageToDo(LotroGame game) {
        Set<PhysicalCard> winners = _skirmishResult.getWinners();
        int dmg = 1;
        ModifiersQuerying modifiersQuerying = game.getModifiersQuerying();
        for (PhysicalCard winner : winners)
            dmg += game.getModifiersQuerying().getKeywordCount(game, winner, Keyword.DAMAGE);

        return dmg;
    }
}
