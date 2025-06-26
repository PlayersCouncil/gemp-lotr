package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

import java.util.Collection;
import java.util.HashSet;

public class BearExtraItemsModifier extends AbstractModifier {
    private final Evaluator _totalExtra;
    private final HashSet<PossessionClass> _classes;

    public BearExtraItemsModifier(PhysicalCard source, Filterable affectFilter, Collection<PossessionClass> classes, Evaluator amount, Condition condition) {
        super(source, "Can bear " + amount + " additional items with the " + String.join(", ", classes.stream().map(PossessionClass::getHumanReadable).toList().toArray(new String[0])) + " class",
                affectFilter, condition, ModifierEffect.CAN_BEAR_EXTRA_ITEMS);
        _totalExtra = amount;
        _classes = new HashSet<>(classes);
    }


    public int getItemClassBonus(LotroGame game, PossessionClass itemClass) {
        if(!_classes.contains(itemClass))
            return 0;

        return _totalExtra.evaluateExpression(game, null);
    }
}
