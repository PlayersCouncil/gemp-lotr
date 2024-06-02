package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.timing.Action;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_V2_017_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("iwilldie", "102_17");
					put("aragorn", "101_19");
					put("veowyn", "4_270");
					put("vgamling", "5_82");

					put("twk", "1_237");
					put("charge", "1_223");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void IWillDieStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: I Will Die as One of Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert Aragorn three times to make each valiant companion strength +2 until the regroup phase. 
		* If Aragorn dies during this turn, make each valiant companion strength +1 for the rest of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("iwilldie");

		assertEquals("I Will Die as One of Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IWillDieExertsAragornThrice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iwilldie = scn.GetFreepsCard("iwilldie");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCardToHand(iwilldie);
		scn.FreepsMoveCharToTable(aragorn);

		var twk = scn.GetShadowCard("twk");
		//A Maneuver event to reset the game state
		var charge = scn.GetShadowCard("charge");
		scn.ShadowMoveCharToTable(twk);
		scn.ShadowMoveCardToHand(charge);

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 1);

		scn.SkipToPhase(Phase.MANEUVER);

		//Need 4 vitality to exert 3 times.  
		assertEquals(3, scn.GetVitality(aragorn));
		assertFalse(scn.FreepsPlayAvailable(iwilldie));

		scn.RemoveWoundsFromChar(aragorn, 1);
		//Although the game state immediately changes, the server has
		// already told the client what actions the player has available, so
		// we need to play until it has a chance to re-evaluate.

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(charge); //Do-nothing maneuver event so they don't both pass

		//Now it's back to Freep's maneuver turn
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(iwilldie));
		scn.FreepsPlayCard(iwilldie);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		//Now that we have ensured that the costs of the card work as they should,
		// further tests can just worry about the effects without re-checking.
	}

	@Test
	public void IWillDiePumpsValiantCompanionsUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iwilldie = scn.GetFreepsCard("iwilldie");
		var aragorn = scn.GetFreepsCard("aragorn");
		var veowyn = scn.GetFreepsCard("veowyn");
		var vgamling = scn.GetFreepsCard("vgamling");
		//Don't forget to test for negatives as well as positives
		var frodo = scn.GetRingBearer();
		scn.FreepsMoveCardToHand(iwilldie);
		//Don't be afraid to cheat and stack the table when we're not explicitly
		// testing for things that need to be played properly.
		scn.FreepsMoveCharToTable(aragorn, veowyn, vgamling);

		var twk = scn.GetShadowCard("twk");
		//That goes for minions too.
		scn.ShadowMoveCharToTable(twk);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(iwilldie));

		// Companion's all have expected strengths before iwilldie is played
		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsPlayCard(iwilldie);

		// Valiant companion's strengths are boosted after card is played
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));
		// Aragorn's and Frodo's is not
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));

		// Ensures it didn't last just the one phase
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));

		//This will bulldoze through the assignments, skipping skirmishes
		scn.SkipToPhase(Phase.REGROUP);

		// Valiant companions have had their strength reduced to normal after the
		// beginning of the regroup phase
		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
	}

	@Test
	public void IWillDiePumpsValiantCompanionsOnDeath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iwilldie = scn.GetFreepsCard("iwilldie");
		var aragorn = scn.GetFreepsCard("aragorn");
		var veowyn = scn.GetFreepsCard("veowyn");
		var vgamling = scn.GetFreepsCard("vgamling");
		var frodo = scn.GetRingBearer();
		scn.FreepsMoveCardToHand(iwilldie);
		scn.FreepsMoveCharToTable(aragorn, veowyn, vgamling);

		var twk = scn.GetShadowCard("twk");
		scn.ShadowMoveCharToTable(twk);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(iwilldie));
		scn.FreepsPlayCard(iwilldie);

		// Valiant companion's strengths are boosted after card is played
		assertEquals(8, scn.GetStrength(veowyn));
		assertEquals(8, scn.GetStrength(vgamling));
		assertEquals(4, scn.GetStrength(frodo));

		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		scn.AddWoundsToChar(aragorn, 1);
		//Dislodging the decision system so the server re-evaluates the game state
		scn.ShadowPassCurrentPhaseAction();

		scn.FreepsResolveRuleFirst();
		assertEquals(Zone.DEAD, aragorn.getZone());

		//Valiant comps boosted by +1 more due to the death
		assertEquals(9, scn.GetStrength(veowyn));
		assertEquals(9, scn.GetStrength(vgamling));
		assertEquals(4, scn.GetStrength(frodo));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Valiant companions continue to have boosted strength outside of Maneuver
		assertEquals(9, scn.GetStrength(veowyn));
		assertEquals(9, scn.GetStrength(vgamling));
		assertEquals(4, scn.GetStrength(frodo));

		// Regroup makes the +2 go away, but the +1 from Aragorn's death should remain
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(7, scn.GetStrength(veowyn));
		assertEquals(7, scn.GetStrength(vgamling));
		assertEquals(4, scn.GetStrength(frodo));

		//Jumps through the Shadow player's freeps turn, and gets us to right after
		// the freeps move to site 3
		scn.SkipToSite(3);

		//All effects have worn off
		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));
		assertEquals(4, scn.GetStrength(frodo));
	}

	@Test
	public void IWillDieDoesNotTriggerOnAragornsDeathNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var iwilldie = scn.GetFreepsCard("iwilldie");
		var aragorn = scn.GetFreepsCard("aragorn");
		var veowyn = scn.GetFreepsCard("veowyn");
		var vgamling = scn.GetFreepsCard("vgamling");
		var frodo = scn.GetRingBearer();
		scn.FreepsMoveCardToHand(iwilldie);
		scn.FreepsMoveCharToTable(aragorn, veowyn, vgamling);

		var twk = scn.GetShadowCard("twk");
		scn.ShadowMoveCharToTable(twk);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(iwilldie);

		scn.SkipToPhase(Phase.REGROUP);
		//Jumps through the Shadow player's freeps turn, and gets us to right after
		// the freeps move to site 3
		scn.SkipToSite(3);

		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));
		assertEquals(4, scn.GetStrength(frodo));

		//put him back so we have a maneuver phase
		scn.ShadowMoveCharToTable(twk);
		scn.AddWoundsToChar(aragorn, 4);
		scn.SkipToPhase(Phase.MANEUVER);

		//Nothing to see here
		assertEquals(Zone.DEAD, aragorn.getZone());
		assertEquals(6, scn.GetStrength(veowyn));
		assertEquals(6, scn.GetStrength(vgamling));
		assertEquals(4, scn.GetStrength(frodo));
	}
}
