package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.NormalSkirmishResult;

import java.util.HashSet;
import java.util.Set;

public class ResolveSkirmishDamageAction extends RequiredTriggerAction {
    private final Set<PhysicalCard> _winners;
    private final Set<PhysicalCard> _losers;

    private Integer _damageToDo;
    private int _remainingDamage;
    // Everyone who is to take these wounds, as the action is made: a loser who is killed leaves the set above
    private final Set<PhysicalCard> _losersAtTheStart;
    // The losers killed part-way whose wounds still to come have been assigned elsewhere (More Yet to Come 10_3)
    private final Set<PhysicalCard> _surplusAssigned = new HashSet<>();

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
        _losersAtTheStart = new HashSet<>(losers);
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

    /**
     * The wounds of this lost skirmish that one loser, killed before the last of them was placed, did not take:
     * the rounds still to come, each of which would have put one wound on him (CR 4.2 "damage bonus": each
     * losing character takes the additional wounds, so each has a share of its own).  0 for a card that is not
     * one of the losers, and once that loser's share has been assigned elsewhere (consumeRemainingDamage).
     * Asked while the kill is being answered, which is between two rounds.
     */
    public int getRemainingDamage(PhysicalCard killedLoser) {
        if (killedLoser == null || !_losersAtTheStart.contains(killedLoser) || _surplusAssigned.contains(killedLoser))
            return 0;
        return _remainingDamage;
    }

    /**
     * Marks one killed loser's wounds still to come as assigned elsewhere.  The rounds go on for the losers still
     * in play: their wounds are their own (F-0475).
     */
    public void consumeRemainingDamage(PhysicalCard killedLoser) {
        if (_losersAtTheStart.contains(killedLoser))
            _surplusAssigned.add(killedLoser);
    }

    private int calculateDamageToDo(LotroGame game) {
        int dmg = 1;
        for (PhysicalCard winner : _winners)
            dmg += game.getModifiersQuerying().getKeywordCount(game, winner, Keyword.DAMAGE);

        return dmg;
    }
}
