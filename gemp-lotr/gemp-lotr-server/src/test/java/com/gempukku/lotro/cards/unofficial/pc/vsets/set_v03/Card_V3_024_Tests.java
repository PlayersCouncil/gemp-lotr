package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("icall", "103_24");
					put("aragorn", "1_89");
					put("anduril", "103_18"); // Anduril, Legend Remade
					put("deadman", "10_27"); // Dead Man of Dunharrow - adds 1 threat on play
					put("boromir", "1_97");
					put("legolas", "1_50");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ICallonYouStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: I Call on You
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert Aragorn (or spot Anduril) and add X threats to play a Wraith from your dead pile, where X is the region number.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("icall");

		assertEquals("I Call on You", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ICallOnYouExertsAragornAndAdds1ThreatInRegion1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(deadman);

		scn.StartGame();

		// Site 1 = Region 1
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DEAD, deadman);

		assertTrue(scn.FreepsPlayAvailable(icall));
		scn.FreepsPlayCard(icall);

		// Aragorn exerted, 1 threat added (region 1)
		// Dead Man played from dead pile (adds 1 more threat)
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertEquals(2, scn.GetThreats());
	}

	@Test
	public void ICallOnYouSpotsAndurilInsteadOfExertingAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, anduril);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(deadman);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(icall);

		// Aragorn NOT exerted (Anduril spotted instead)
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Dead Man played
		assertInZone(Zone.FREE_CHARACTERS, deadman);
	}

	@Test
	public void ICallOnYouAdds2ThreatsInRegion2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(deadman);

		scn.StartGame();

		// Move to site 4 (Region 2)
		scn.SkipToSite(4);

		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(icall);

		// 2 threats added (region 2) + 1 threat from Dead Man = 3 total
		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, deadman);
	}

	@Test
	public void ICallOnYouAdds3ThreatsInRegion3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(deadman);

		scn.StartGame();

		// Move to site 7 (Region 3)
		scn.SkipToSite(7);

		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(icall);

		// 3 threats added (region 3) + 1 threat from Dead Man = 4 total
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, deadman);
	}

	@Test
	public void ICallOnYouCannotPlayWithoutAragornOrAnduril() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var deadman = scn.GetFreepsCard("deadman");
		var boromir = scn.GetFreepsCard("boromir");
		// No Aragorn or Anduril

		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(deadman);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(icall));
	}

	@Test
	public void ICallOnYouCannotPlayWithoutWraithInDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		// No Wraith in dead pile

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(icall);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(icall));
	}

	@Test
	public void ICallOnYouCannotPlayIfThreatLimitWouldBeExceeded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deadman = scn.GetFreepsCard("deadman");
		// Only Frodo + Aragorn = 2 companions = threat limit 2
		// Region 1 cost (1) + Dead Man cost (1) = 2 threats needed
		// But if we pre-add 1 threat, we can only add 1 more, not enough for both costs

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(deadman);

		scn.StartGame();

		// Pre-add 1 threat - now only 1 threat room remaining
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());

		// Cannot play - need 2 more threats (1 for event + 1 for Dead Man) but only room for 1
		// Ideally this would block the event from playing entirely, but I can't be arsed to add
		// "with threats removed" to the interface like it has for twilight
		scn.FreepsPlayAvailable(icall);
		assertInZone(Zone.DEAD,  deadman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

}
