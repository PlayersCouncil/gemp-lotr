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

public class Card_13_190_Tests
{
	// An unordered Shadows-block path that starts at the Doors, so that its text is in force in the first
	// fellowship phase, where companions can be played.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "103_31");        // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3; enduring, with no wound here
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability never used
					put("guard", "5_83");           // Household Guard: [rohan] Man companion, strength 6, valiant and nothing else
					put("boromir", "1_97");         // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability never used
					put("barliman", "1_70");        // Barliman Butterbur: [gandalf] Man ally, strength 1; a fellowship ability never used
					put("guma", "4_277");           // Guma, Plains Farmer: [rohan] Man ally, strength 4; nobody controls his home site
					put("brigand1", "4_10");        // Dunlending Brigand: [dunland] Man minion, strength 8, no text
					put("brigand2", "4_10");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site1", "13_190");         // the Doors in place of Fangorn Glade; Sirannon Ruins 18_138 (River, no other text) is site5
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DoorsofDurinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Doors of Durin
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Marsh</b>. Each companion of each race that has the most companions is strength -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("Doors of Durin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
	}

	@Test
	public void EachCompanionOfTheRaceWithTheMostCompanionsIsWeakerAndNoAllyOrMinionCountsOrIsWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var guma = scn.GetFreepsCard("guma");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(barliman, guma);
		scn.MoveMinionsToTable(brigand1, brigand2);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Doors of Durin", scn.GetCurrentSite().getBlueprint().getTitle());

		// Hobbit companions 2 (Frodo [shire], Pippin [gondor]), Man companions 1 (Aragorn). The two Man allies and the
		// two Man minions are no companions: counted, the Men would have 3 or 5 and the Hobbits would be spared.
		// Frodo 3 + 1 Ruling Ring - 1; Pippin 3 - 1.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(pippin));
		// The race that does not have the most, and the allies and minions, are at their printed strength.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetStrength(barliman));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(8, scn.GetStrength(brigand2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoRacesTiedForTheMostEachCompanionOfBothIsWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(pippin, aragorn, guard);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Doors of Durin", scn.GetCurrentSite().getBlueprint().getTitle());

		// Hobbits 2 (Frodo, Pippin), Men 2 (Aragorn [gondor], the Guard [rohan]): each race has the most.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CompanionsPlayedAtTheDoorsChangeWhichRaceHasTheMost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToHand(guard, boromir);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Doors of Durin", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInHand(guard, boromir);

		// Hobbits 2, Men 1.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(guard);

		// Hobbits 2, Men 2: both races have the most.
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(guard));

		scn.FreepsPlayCard(boromir);

		// Men 3, Hobbits 2: the Men alone have the most.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheDoorsNoCompanionIsWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertEquals("Doors of Durin", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));

		// At Sirannon Ruins every companion is at his printed strength again (Frodo 3 + 1 Ruling Ring).
		assertEquals("Sirannon Ruins", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
