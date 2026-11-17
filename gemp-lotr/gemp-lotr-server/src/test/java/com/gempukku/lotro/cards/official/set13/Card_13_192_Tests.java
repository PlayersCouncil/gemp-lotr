package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_192_Tests
{
	// An unordered Shadows-block path that starts at the Gates, so that its text is in force in the first
	// fellowship phase, where a companion can be played.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability never used
					put("guard", "5_83");           // Household Guard: [rohan] Man companion, strength 6, valiant and nothing else
					put("barliman", "1_70");        // Barliman Butterbur: [gandalf] Man ally, strength 1; a fellowship ability never used
					put("brigand", "4_10");         // Dunlending Brigand: [dunland] Man minion, strength 8, no text
					put("raiders", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai minion, strength 9, damage +1 only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "13_192");         // the Gates in place of Fangorn Glade; Sirannon Ruins 18_138 (River, no other text) is site5
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void TheGreatGatesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: The Great Gates
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. While a player can spot more companions than minions, each minion is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("The Great Gates", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void WithMoreCompanionsThanMinionsEachMinionIsStrongerAndNoCompanionOrAllyIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var barliman = scn.GetFreepsCard("barliman");
		var brigand = scn.GetShadowCard("brigand");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveAlliesToTable(barliman);
		// Laid early: minions stand beside these companions later in the turn (Q-0228).
		scn.MoveMinionsToTable(brigand, raiders);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("The Great Gates", scn.GetCurrentSite().getBlueprint().getTitle());

		// Three companions (Frodo, Aragorn, the Guard) against two minions: exactly one more.
		// Each minion, of two cultures and two races, is +1.
		assertEquals(9, scn.GetStrength(brigand));
		assertEquals(10, scn.GetStrength(raiders));
		// The companions and the ally are at their printed strength (Frodo 3 + 1 Ruling Ring).
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(1, scn.GetStrength(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAsManyCompanionsAsMinionsNoMinionIsStrongerAndAnAllyDoesNotCountAsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var brigand = scn.GetShadowCard("brigand");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveMinionsToTable(brigand, raiders);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("The Great Gates", scn.GetCurrentSite().getBlueprint().getTitle());

		// Two companions (Frodo, Aragorn) against two minions: not more. Barliman, an ally, counted as a
		// companion would make it 3 against 2.
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(9, scn.GetStrength(raiders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionPlayedAtTheGatesMakesEachMinionStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var brigand = scn.GetShadowCard("brigand");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guard);
		scn.MoveMinionsToTable(brigand, raiders);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("The Great Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInHand(guard);

		// Two companions against two minions.
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(9, scn.GetStrength(raiders));

		scn.FreepsPlayCard(guard);

		// Three against two.
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(9, scn.GetStrength(brigand));
		assertEquals(10, scn.GetStrength(raiders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheGatesNoMinionIsStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var brigand = scn.GetShadowCard("brigand");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveMinionsToTable(brigand, raiders);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("The Great Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(9, scn.GetStrength(brigand));
		assertEquals(10, scn.GetStrength(raiders));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));

		// At Sirannon Ruins, still three companions against two minions, each minion at its printed strength.
		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(9, scn.GetStrength(raiders));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
