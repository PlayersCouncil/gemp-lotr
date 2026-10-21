package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "2_68");
					// The Balrog, The Terror of Khazad-dum: vitality 5; his text acts only underground, which site 2 is not
					put("balrog", "12_79");
					// Goblin Patrol Troop: a [moria] minion with no text, no Balrog
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MustDoWithoutHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Must Do Without Hope
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert The Balrog to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Must Do Without Hope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverEventExertsTheBalrogToAddABurdenToTheFreePeoplesRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, troop);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertTrue(scn.ShadowPlayAvailable(hope));

		scn.ShadowPlayCard(hope);

		// The Balrog exerts, and the Troop beside him, who is no Balrog, does not.  The burden goes on the Free
		// Peoples player's Ring-bearer; the Shadow player's own has none.
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertInZone(Zone.DISCARD, hope);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheBalrogIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetShadowCard("hope");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, troop);
		// Vitality 5 with 4 wounds: exhausted.  The Troop beside him could exert, but is no Balrog.
		scn.AddWoundsToChar(balrog, 4);
		assertEquals(4, scn.GetWoundsOn(balrog));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInHand(hope);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hope));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.FreepsGetBurdens());
		assertInHand(hope);
	}
}
