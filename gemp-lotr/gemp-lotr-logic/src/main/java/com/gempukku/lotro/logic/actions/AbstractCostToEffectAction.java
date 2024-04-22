package com.gempukku.lotro.logic.actions;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.DiscountEffect;
import com.gempukku.lotro.logic.timing.Effect;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class AbstractCostToEffectAction implements CostToEffectAction {
    private final LinkedList<DiscountEffect> _potentialDiscounts = new LinkedList<>();
    private final LinkedList<DiscountEffect> _processedDiscounts = new LinkedList<>();
    private final LinkedList<Effect> _costs = new LinkedList<>();
    private final LinkedList<Effect> _processedCosts = new LinkedList<>();
    private final LinkedList<Effect> _effects = new LinkedList<>();
    private final LinkedList<Effect> _processedEffects = new LinkedList<>();

    private String text;

    private Phase _actionTimeword;
    private String _performingPlayer;

    private boolean _virtualCardAction = false;
    private boolean _paidToil = false;

    public boolean isPaidToil() {
        return _paidToil;
    }

    @Override
    public void setPaidToil(boolean paidToil) {
        _paidToil = paidToil;
    }

    @Override
    public void setVirtualCardAction(boolean virtualCardAction) {
        _virtualCardAction = virtualCardAction;
    }

    @Override
    public boolean isVirtualCardAction() {
        return _virtualCardAction;
    }

    @Override
    public Phase getActionTimeword() {
        return _actionTimeword;
    }

    @Override
    public void setActionTimeword(Phase phase) {
        _actionTimeword = phase;
    }

    @Override
    public void setPerformingPlayer(String playerId) {
        _performingPlayer = playerId;
    }

    @Override
    public String getPerformingPlayer() {
        return _performingPlayer;
    }

    @Override
    public final void appendPotentialDiscount(DiscountEffect discount) {
        _potentialDiscounts.add(discount);
    }

    @Override
    public final void appendCost(Effect cost) {
        _costs.add(cost);
    }

    @Override
    public final void appendEffect(Effect effect) {
        _effects.add(effect);
    }

    @Override
    public final void insertCost(Effect... cost) {
        _costs.addAll(0, Arrays.asList(cost));
    }

    @Override
    public final void insertEffect(Effect... effect) {
        _effects.addAll(0, Arrays.asList(effect));
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText(LotroGame game) {
        return text;
    }

    protected boolean isCostFailed() {
        for (Effect processedCost : _processedCosts) {
            if (!processedCost.wasCarriedOut())
                return true;
        }
        return false;
    }

    protected int getProcessedDiscount() {
        int discount = 0;
        for (DiscountEffect processedDiscount : _processedDiscounts) {
            discount += processedDiscount.getDiscountPaidFor();
        }
        return discount;
    }

    protected int getPotentialDiscount(LotroGame game) {
        int sum = 0;
        for (DiscountEffect potentialDiscount : _potentialDiscounts) {
            sum += potentialDiscount.getMaximumPossibleDiscount(game);
        }
        return sum;
    }

    protected final boolean hasNextCost() {
        return !_costs.isEmpty();
    }

    protected final Effect getNextCost() {
        Effect cost = _costs.poll();
        if (cost != null)
            _processedCosts.add(cost);
        return cost;
    }

    protected final boolean hasNextEffect() {
        return !_effects.isEmpty();
    }

    protected final Effect getNextEffect() {
        final Effect effect = _effects.poll();
        if (effect != null)
            _processedEffects.add(effect);
        return effect;
    }

    protected final DiscountEffect getNextPotentialDiscount() {
        DiscountEffect discount = _potentialDiscounts.poll();
        if (discount != null)
            _processedDiscounts.add(discount);
        return discount;
    }

    public boolean wasCarriedOut() {
        if (isCostFailed())
            return false;

        for (Effect processedEffect : _processedEffects) {
            if (!processedEffect.wasCarriedOut())
                return false;
        }

        return true;
    }
}
