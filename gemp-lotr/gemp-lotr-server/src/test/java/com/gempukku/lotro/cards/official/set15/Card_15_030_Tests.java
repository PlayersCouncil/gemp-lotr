package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaflock", "15_30");
					// Ent Avenger: Ent companion, cost 4, not unique; strength +2 only with 3 Ents. One to play
					// and one to sit in the hand beside Leaflock, its cost changed by nothing
					put("avenger", "6_27");
					put("avenger2", "6_27");
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: the Ent who is an ally
					put("onod", "17_26");       // Woodland Onod: Ent companion, strength 7; a skirmish ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Ent
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeaflockStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Leaflock, Finglas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 7
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: Leaflock's twilight cost is -1 for each Ent you can spot.<br>While you can spot 4 other Ents, Leaflock is strength +4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaflock");

		assertEquals("Leaflock", card.getBlueprint().getTitle());
		assertEquals("Finglas", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CostsOneLessForEachEntCompanionOrAllyInPlayAndNothingForAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaflock = scn.GetFreepsCard("leaflock");
		var avenger = scn.GetFreepsCard("avenger");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(leaflock, avenger, avenger2);

		scn.StartGame();

		// Frodo and Gandalf are no Ents: nothing off. The Ent Avenger in the same hand costs its printed 4.
		assertEquals(7, scn.GetTwilightCost(leaflock));
		assertEquals(4, scn.GetTwilightCost(avenger2));

		scn.FreepsPlayCard(avenger);
		assertInZone(Zone.FREE_CHARACTERS, avenger);

		// One Ent companion in play.
		assertEquals(6, scn.GetTwilightCost(leaflock));
		assertEquals(4, scn.GetTwilightCost(avenger2));

		scn.MoveAlliesToTable(treebeard);
		scn.FreepsRefreshDecision();

		// An Ent companion and an Ent ally.
		assertEquals(5, scn.GetTwilightCost(leaflock));
		assertEquals(4, scn.GetTwilightCost(avenger2));

		assertEquals(4, scn.GetTwilight());
		scn.FreepsPlayCard(leaflock);
		// 4 from the Ent Avenger + (7 - 2)
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, leaflock);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlus4OnceAFourthOtherEntIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaflock = scn.GetFreepsCard("leaflock");
		var avenger = scn.GetFreepsCard("avenger");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var treebeard = scn.GetFreepsCard("treebeard");
		var onod = scn.GetFreepsCard("onod");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(leaflock, avenger, onod, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(avenger2);

		scn.StartGame();

		// Four Ents counting Leaflock himself, three other: an Ent Avenger, Woodland Onod and Treebeard the ally.
		// Gandalf is no Ent.
		assertEquals(8, scn.GetStrength(leaflock));
		assertEquals(7, scn.GetStrength(onod));

		scn.FreepsPlayCard(avenger2);
		assertInZone(Zone.FREE_CHARACTERS, avenger2);

		// Four other Ents, the ally among them. The bonus is Leaflock's alone.
		assertEquals(12, scn.GetStrength(leaflock));
		assertEquals(7, scn.GetStrength(onod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
