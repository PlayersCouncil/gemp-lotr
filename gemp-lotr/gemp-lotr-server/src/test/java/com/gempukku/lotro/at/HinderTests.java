package com.gempukku.lotro.at;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.HinderCardsInPlayEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class HinderTests
{
    /**
     * Each of these functions follows the same basic pattern: hack in a special ability on Frodo
     * (the 12-resistance once with no other abilities) which hinders Aragorn by name, then activate
     * the ability and ensure that Aragorn is unable to perform some function that hindered companions
     * should be barred from.
     */
    @Test
    public void HinderingACompanionInPlayFlipsItOver() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertFalse(aragorn.isFlipped());

        scn.FreepsUseCardAction(frodo);

        assertTrue(aragorn.isFlipped());
    }

    @Test
    public void HinderingACompanionInPlayGrantsItTheHinderedStatus() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        var scn = new VirtualTableScenario(
                new HashMap<>() {{
                    put("aragorn", "1_89");
                }}
        );

        var frodo = scn.GetRingBearer();
        var aragorn = scn.GetFreepsCard("aragorn");
        scn.MoveCompanionsToTable(aragorn);

        scn.ApplyAdHocAction(new AbstractActionProxy() {
            @Override
            public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
                RequiredTriggerAction action = new RequiredTriggerAction(frodo);
                action.appendEffect(new HinderCardsInPlayEffect(null, null, aragorn));
                action.setText("Hinder Aragorn");
                return Collections.singletonList(action);
            }
        });
        scn.StartGame();

        assertFalse(aragorn.isFlipped());
        assertFalse(scn.IsHindered(aragorn));
        assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());

        scn.FreepsUseCardAction(frodo);

        assertTrue(aragorn.isFlipped());
        assertTrue(scn.IsHindered(aragorn));
    }

    //@Test
    public void CannotBeHinderedModifierPreventsTargetFromBeingHindered() throws DecisionResultInvalidException, CardNotFoundException {

    }



    //@Test
    public void HinderedCompanionsCanBeSpottedAsFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
        //Courtesy of my Hall
    }

}
