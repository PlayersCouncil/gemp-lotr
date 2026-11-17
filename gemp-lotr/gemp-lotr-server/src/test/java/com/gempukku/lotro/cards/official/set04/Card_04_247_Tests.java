package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_247_Tests
{

	// The minion archery total is read with GetShadowArcheryTotal, the calculation the archery phase uses; no arrows
	// are fired.  None of the minions below is an archer but by the Bow.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "4_247");

					// Easterling Infantry: [raider] Easterling Man; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
					// Easterling Axeman: [raider] Easterling Man, cost 2; +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Southron Explorer: [raider] Southron Man; a regroup ability only
					put("explorer", "4_250");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul); his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Bow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be a [raider] Man.<br>Bearer is an <b>archer</b>.<br>While you can spot another [raider] Man, the minion archery total is +1.
		*/

		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");

		assertEquals("Southron Bow", bow.getBlueprint().getTitle());
		assertNull(bow.getBlueprint().getSubtitle());
		assertFalse(bow.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, bow.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, bow.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, bow.getBlueprint().getCardType());
		assertTrue(bow.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(3, bow.getBlueprint().getTwilightCost());
	}


	@Test
	public void PlaysOnARaiderManOnlyAndMakesHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var infantry = scn.GetShadowCard("infantry");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(bow);
		assertInHand(bow);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry, explorer, khamul, brigand);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(explorer, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(infantry, Keyword.ARCHER));
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowPlayAvailable(bow));

		scn.ShadowPlayCard(bow);
		// An Easterling and a Southron, [raider] Men both; not the [raider] Nazgul, the [dunland] Man or Frodo.
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(infantry);

		assertAttachedTo(bow, infantry);
		assertTrue(scn.HasKeyword(infantry, Keyword.ARCHER));
		// The other [raider] Man, who does not bear it, is no archer.
		assertFalse(scn.HasKeyword(explorer, Keyword.ARCHER));
		// 1 for the archer + 1 while another [raider] Man (the Southron Explorer) is spotted.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBearerIsNoOtherRaiderManAndTheTotalIsRaisedOnceAnotherIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(axeman);

		scn.StartGame();
		assertInHand(axeman);
		scn.MoveMinionsToTable(explorer, khamul, brigand);
		scn.AttachCardsTo(explorer, bow);
		assertAttachedTo(bow, explorer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The bearer is the only [raider] Man: the [raider] Nazgul is no Man, the Brigand is of another culture.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(explorer, Keyword.ARCHER));
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		// 1 for the archer + 1 while another [raider] Man (the Easterling Axeman) is spotted.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
