package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.InactiveReason;
import com.gempukku.lotro.common.SpotOverride;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.AbstractEffect;

import java.util.*;

public abstract class AbstractPreventableCardEffect extends AbstractEffect implements PreventableCardEffect {
    private final Filter _filter;
    private final Map<InactiveReason, Boolean> _spotOverrides = new HashMap<>();
    private final Set<PhysicalCard> _preventedTargets = new HashSet<>();
    private final Set<PhysicalCard> _replacedTargets = new HashSet<>();
    private int _requiredTargets;

    public AbstractPreventableCardEffect(PhysicalCard... cards) { this(SpotOverride.NONE, cards); }

    public AbstractPreventableCardEffect(Map<InactiveReason, Boolean> spotOverrides, PhysicalCard... cards) {
        List<PhysicalCard> affectedCards = Arrays.asList(cards);
        _requiredTargets = affectedCards.size();
        _filter = Filters.in(affectedCards);
        _spotOverrides.putAll(spotOverrides);
    }

    public AbstractPreventableCardEffect(Filterable... filter) { this(SpotOverride.NONE, filter); }

    public AbstractPreventableCardEffect(Map<InactiveReason, Boolean> spotOverrides, Filterable... filter) {
        _spotOverrides.putAll(spotOverrides);
        _filter = Filters.and(filter);
    }

    protected abstract Filter getExtraAffectableFilter();

    protected final Collection<PhysicalCard> getAffectedCards(LotroGame game) {
        return Filters.filterActive(game, _spotOverrides, _filter, getExtraAffectableFilter());
    }

    public final Collection<PhysicalCard> getAffectedCardsMinusPrevented(LotroGame game) {
        Collection<PhysicalCard> affectedCards = getAffectedCards(game);
        affectedCards.removeAll(_preventedTargets);
        affectedCards.removeAll(_replacedTargets);
        return affectedCards;
    }

    @Override
    public boolean isPlayableInFull(LotroGame game) {
        return getAffectedCardsMinusPrevented(game).size() + _replacedTargets.size() >= _requiredTargets;
    }

    protected abstract void playoutEffectOn(LotroGame game, Collection<PhysicalCard> cards);

    @Override
    protected FullEffectResult playEffectReturningResult(LotroGame game) {
        Collection<PhysicalCard> affectedMinusPreventedCards = getAffectedCardsMinusPrevented(game);
        playoutEffectOn(game, affectedMinusPreventedCards);
        return new FullEffectResult(affectedMinusPreventedCards.size() + _replacedTargets.size() >= _requiredTargets);
    }

    public void preventEffect(LotroGame game, PhysicalCard card) {
        _preventedTargets.add(card);
        _prevented = true;
    }

    /**
     * Something else happens to the card in place of this effect ("... add a burden instead"): the card is left
     * out of the effect as a prevented card is, and nothing further answers it as about to be affected, but the
     * effect is not prevented by it.  It counts as done for that card, so a cost paid this way is paid and an
     * "if you do" goes ahead (CR 4.2 "instead": "This does not mean that the original effect is prevented";
     * Ketura, 2026-10-09: "instead" converts a cost; F-0507).
     * @param game The game
     * @param card A card among those the effect is still to affect
     */
    protected void replaceEffect(LotroGame game, PhysicalCard card) {
        _replacedTargets.add(card);
    }
}
