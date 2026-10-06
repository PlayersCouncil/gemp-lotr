package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.NormalSkirmishResult;

import java.util.Set;

public class ResolveSkirmishDamageAction extends RequiredTriggerAction {
    private final Set<PhysicalCard> _winners;
    private final Set<PhysicalCard> _losers;

    private Integer _damageToDo;
    private int _remainingDamage;

    public ResolveSkirmishDamageAction(NormalSkirmishResult skirmishResult) {
        this(skirmishResult.getWinners(), skirmishResult.getInSkirmishLosers());
    }

    /**
     * @param losers the characters in the skirmish who take the wounds for losing it
     */
    public ResolveSkirmishDamageAction(Set<PhysicalCard> winners, Set<PhysicalCard> losers) {
        super(null);
        setText("Resolve skirmish damage");
        _winners = winners;
        _losers = losers;
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
            return new WoundCharactersEffect(_winners, Filters.in(_losers),
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
        int dmg = 1;
        for (PhysicalCard winner : _winners)
            dmg += game.getModifiersQuerying().getKeywordCount(game, winner, Keyword.DAMAGE);

        return dmg;
    }
}
