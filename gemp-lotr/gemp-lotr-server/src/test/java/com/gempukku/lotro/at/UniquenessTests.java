package com.gempukku.lotro.at;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class UniquenessTests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("rotn", "1_89");
                    put("kie", "1_365");
                    put("merry1", "1_302");
                    put("merry2", "1_302");
                    put("sting1", "1_313");
                    put("sting2", "1_313");
                    put("sword1", "1_299");
                    put("sword2", "1_299");
                    put("guest1", "1_297");
                    put("guest2", "1_297");
                    put("guest3", "1_297");
                    put("guest4", "1_297");

                    put("enqueaaccent", "1_231");
                    put("enqueanoaccent", "60_68");
                }}
        );
    }

    @Test
    public void CopiesOfSameUniqueCompanionCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var merry1 = scn.GetFreepsCard("merry1");
        var merry2 = scn.GetFreepsCard("merry2");

        scn.MoveCardsToHand(merry1, merry2);

        scn.StartGame();

        assertTrue(scn.FreepsPlayAvailable(merry1));
        scn.FreepsPlayCard(merry1);
        assertFalse(scn.FreepsPlayAvailable(merry2));
    }

    @Test
    public void CopiesOfDifferentUniqueCompanionCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var rotn = scn.GetFreepsCard("rotn");
        var kie = scn.GetFreepsCard("kie");

        scn.MoveCardsToHand(rotn, kie);

        scn.StartGame();

        assertTrue(scn.FreepsPlayAvailable(rotn));
        assertTrue(scn.FreepsPlayAvailable(kie));
        scn.FreepsPlayCard(rotn);
        assertFalse(scn.FreepsPlayAvailable(kie));
    }

    @Test
    public void CopiesOfSameUniquePossessionCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var sting1 = scn.GetFreepsCard("sting1");
        var sting2 = scn.GetFreepsCard("sting2");


        scn.MoveCardsToHand(sting1, sting2);

        scn.StartGame();

        assertTrue(scn.FreepsPlayAvailable(sting1));
        scn.FreepsPlayCard(sting1);
        assertFalse(scn.FreepsPlayAvailable(sting2));
    }

    @Test
    public void CopiesOfSameNonUniquePossessionCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var merry = scn.GetFreepsCard("merry1");
        var sword1 = scn.GetFreepsCard("sword1");
        var sword2 = scn.GetFreepsCard("sword2");

        scn.MoveCompanionsToTable(merry);
        scn.MoveCardsToHand(sword1, sword2);

        scn.StartGame();

        assertTrue(scn.FreepsPlayAvailable(sword1));
        scn.FreepsPlayCard(sword1);
        scn.FreepsChooseCard(merry);
        assertTrue(scn.FreepsPlayAvailable(sword2));
    }

    @Test
    public void CopiesOfSameNonUniqueAlliesCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var guest1 = scn.GetFreepsCard("guest1");
        var guest2 = scn.GetFreepsCard("guest2");
        var guest3 = scn.GetFreepsCard("guest3");
        var guest4 = scn.GetFreepsCard("guest4");

        scn.MoveCardsToHand(guest1, guest2, guest3, guest4);

        scn.StartGame();

        assertTrue(scn.FreepsPlayAvailable(guest1));
        scn.FreepsPlayCard(guest1);
        assertTrue(scn.FreepsPlayAvailable(guest2));
        scn.FreepsPlayCard(guest2);
        assertTrue(scn.FreepsPlayAvailable(guest3));
        scn.FreepsPlayCard(guest3);
        assertTrue(scn.FreepsPlayAvailable(guest4));
        scn.FreepsPlayCard(guest4);
    }

    //This tests whether or not characters of equivalent names that aren't 100% the same still count
    // as the same for uniqueness purposes.  In this case, that Úlairë Enquëa == Ulaire Enquea
    @Test
    public void CopiesOfDifferentUniqueMinionWithAccentCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        var enqueaaccent = scn.GetShadowCard("enqueaaccent");
        var enqueanoaccent = scn.GetShadowCard("enqueanoaccent");

        scn.MoveCardsToHand(enqueaaccent, enqueanoaccent);

        scn.StartGame();

        scn.SetTwilight(20);

        scn.FreepsPassCurrentPhaseAction();

        assertTrue(scn.ShadowPlayAvailable(enqueaaccent));
        assertTrue(scn.ShadowPlayAvailable(enqueanoaccent));
        scn.ShadowPlayCard(enqueaaccent);
        assertFalse(scn.ShadowPlayAvailable(enqueanoaccent));
    }
}
