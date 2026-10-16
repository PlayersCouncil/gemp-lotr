package com.gempukku.lotro.logic.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.PhysicalCard;

/**
 * "<Such a character> comes into play exhausted" (Goblin Domain 1_175, Blood of Númenor 2_31, Race Text 91_9, Not
 * at Home 30_37).  It does nothing to a card in play: PlayCardEffect asks for it (ModifiersQuerying
 * .comesIntoPlayExhausted) as it places a character, and lays the wounds that leave it 1 vitality before its play is
 * answered (Q-0302, F-0501).
 */
public class ComesIntoPlayExhaustedModifier extends AbstractModifier {
    public ComesIntoPlayExhaustedModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "Comes into play exhausted", affectFilter, condition, ModifierEffect.COMES_INTO_PLAY_EXHAUSTED_MODIFIER);
    }
}
