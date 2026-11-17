package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_208_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cry", "7_208");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; nothing below 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl; nothing below 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a companion
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion; a skirmish ability used only on purpose
					// Goblin Patrol Troop: no text.  Cards for the Free Peoples hand, whose size decides the initiative.
					put("hand1", "1_177");
					put("hand2", "1_177");
					put("hand3", "1_177");
					put("hand4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThereCameaCryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: There Came a Cry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot a Nazgûl to add a threat (or 3 threats if you have initiative).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cry");

		assertEquals("There Came a Cry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAWraithNazgulSpottedAndFourCardsInTheFreePeoplesHandItAddsOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		scn.MoveCardsToHand(cry);
		assertInHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		// Four cards in the Free Peoples hand: he has the initiative, and the Shadow does not.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetThreats());
		assertInHand(cry);
		assertTrue(scn.ShadowPlayAvailable(cry));

		scn.ShadowPlayCard(cry);

		// One threat; four companions would have room for three.
		assertEquals(1, scn.GetThreats());
		assertInDiscard(cry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithARaiderNazgulSpottedAndThreeCardsInTheFreePeoplesHandItAddsThreeThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.MoveCardsToHand(cry);
		assertInHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.FreepsPass();

		// Three cards in the Free Peoples hand: the Shadow has the initiative.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetThreats());
		assertInHand(cry);
		assertTrue(scn.ShadowPlayAvailable(cry));

		scn.ShadowPlayCard(cry);

		// Frodo and three companions: room for all three threats.
		assertEquals(3, scn.GetThreats());
		assertInDiscard(cry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAWraithMinionWhoIsNoNazgulUntilANazgulIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var hound = scn.GetShadowCard("hound");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"));
		scn.MoveCardsToHand(cry);
		assertInHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(cry);
		assertFalse(scn.ShadowPlayAvailable(cry));

		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(cry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
