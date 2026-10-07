package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_047_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("initiate1", "103_47");
					put("initiate2", "103_47");
					put("soldier", "4_254");    // Southron Soldier: [raider] Southron, cost 2, site 4; ambush (2) and no other text
					put("nomad", "7_132");      // Desert Nomad: [raider] Southron, cost 4, site 4; nothing more below 2 threats
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man who is no Southron, cost 2, site 4; nothing more below 2 burdens
					put("orc", "1_271");        // Orc Soldier: [sauron] Orc, cost 2, site 6; a skirmish ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindInitiateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Initiate
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron.</b>  Tracker. Ambush (3).
		 * 	Other Southrons are twilight cost -1.
		 * 	When you play this minion, remove (3) or hinder this minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("initiate1");

		assertEquals("Desert Wind Initiate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedForOneAndTwoRoamingThenRemovingThreeLeavesItUnhindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate1");
		scn.MoveCardsToHand(initiate);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 3 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.HAND, initiate);
		assertTrue(scn.ShadowPlayAvailable(initiate));

		scn.ShadowPlayCard(initiate);

		// 6 - 1 to play - 2 roaming (a site 4 minion played at site 2): no other Initiate is there to lower
		// its cost. That leaves exactly the 3 the first alternative needs.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, initiate);
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Remove (3)");

		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsHindered(initiate));
		assertInZone(Zone.SHADOW_CHARACTERS, initiate);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToHinderRemovesNothingAndWhileItIsHinderedASouthronCostsWhatItSays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate1");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(initiate, soldier);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(initiate);

		// 10 - 1 to play - 2 roaming
		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.IsHindered(initiate));
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Hinder");

		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.IsHindered(initiate));
		assertInZone(Zone.SHADOW_CHARACTERS, initiate);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(soldier);

		// The game text of a hindered card does not apply: 7 - 2 to play - 2 roaming, with nothing off.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyTwoLeftInThePoolItIsHinderedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate1");
		scn.MoveCardsToHand(initiate);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.IsHindered(initiate));

		scn.ShadowPlayCard(initiate);

		// 5 - 1 to play - 2 roaming leaves 2, one short of what removing (3) needs.
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.IsHindered(initiate));
		assertInZone(Zone.SHADOW_CHARACTERS, initiate);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASouthronOfAnyCultureCostsOneLessAndARaiderManWhoIsNoSouthronDoesNot() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate1");
		var soldier = scn.GetShadowCard("soldier");
		var orc = scn.GetShadowCard("orc");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(soldier, orc, axeman);
		// Every Southron there is is a [raider] Man: the Orc Soldier is made one to tell the keyword from the culture.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, scn.GenerateShadowFilter("name(Orc Soldier)"), null, Keyword.SOUTHRON));

		scn.StartGame();
		scn.MoveMinionsToTable(initiate);
		scn.SetTwilight(12);
		scn.FreepsPass();

		// 12 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(15, scn.GetTwilight());
		assertFalse(scn.IsHindered(initiate));

		scn.ShadowPlayCard(soldier);
		// 15 - (2 - 1) to play - 2 roaming
		assertEquals(12, scn.GetTwilight());

		scn.ShadowPlayCard(orc);
		// 12 - (2 - 1) to play - 2 roaming (a site 6 minion)
		assertEquals(9, scn.GetTwilight());

		scn.ShadowPlayCard(axeman);
		// 9 - 2 to play - 2 roaming: an Easterling, not a Southron
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, orc, axeman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondInitiateIsLoweredByTheFirstAndOnlyTheOnePlayedMakesItsChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate1 = scn.GetShadowCard("initiate1");
		var initiate2 = scn.GetShadowCard("initiate2");
		var nomad = scn.GetShadowCard("nomad");
		scn.MoveCardsToHand(initiate2, nomad);

		scn.StartGame();
		scn.MoveMinionsToTable(initiate1);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(initiate2);

		// 10 - (1 - 1) to play - 2 roaming: the first Initiate lowers the second.
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowChoiceAvailable("Remove (3)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Hinder");

		// The copy played is the one hindered, and the copy already in play is asked nothing.
		assertTrue(scn.IsHindered(initiate2));
		assertFalse(scn.IsHindered(initiate1));
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(nomad);

		// 8 - (4 - 1) to play - 2 roaming: one Initiate's text applies, the hindered one's does not.
		// Playing another minion sets neither Initiate's choice off.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.IsHindered(initiate1));
		assertInZone(Zone.SHADOW_CHARACTERS, nomad);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoInitiatesEachLowerAnotherSouthronsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate1 = scn.GetShadowCard("initiate1");
		var initiate2 = scn.GetShadowCard("initiate2");
		var nomad = scn.GetShadowCard("nomad");
		scn.MoveCardsToHand(nomad);

		scn.StartGame();
		scn.MoveMinionsToTable(initiate1, initiate2);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(nomad);

		// 10 - (4 - 1 - 1) to play - 2 roaming
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nomad);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASouthronAlreadyInPlayStillCostsTheNumberPrintedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var initiate = scn.GetShadowCard("initiate1");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(initiate);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilightCost(soldier));

		scn.ShadowPlayCard(initiate);
		scn.ShadowChoose("Remove (3)");

		assertFalse(scn.IsHindered(initiate));
		// The Soldier is not coming into play: its cost is the 2 printed on it.
		assertEquals(2, scn.GetTwilightCost(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
