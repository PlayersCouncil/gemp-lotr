package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_155_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "7_155");

					// Easterling Infantry: [raider] Easterling Man; fierce and damage +1 only at 3 burdens
					put("infantry", "4_227");
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
	public void RaiderBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Raider Bow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be a [raider] Man.<br>Bearer is an <b>archer</b> and <b>ambush</b> (5).
		*/

		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");

		assertEquals("Raider Bow", bow.getBlueprint().getTitle());
		assertNull(bow.getBlueprint().getSubtitle());
		assertFalse(bow.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, bow.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, bow.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, bow.getBlueprint().getCardType());
		assertTrue(bow.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, bow.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARaiderManOnlyAndMakesHimAnArcherWithAmbushFive() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertEquals(0, scn.GetKeywordCount(infantry, Keyword.AMBUSH));
		assertTrue(scn.ShadowPlayAvailable(bow));

		scn.ShadowPlayCard(bow);
		// An Easterling and a Southron, [raider] Men both; not the [raider] Nazgul, the [dunland] Man or Frodo.
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(explorer);

		assertAttachedTo(bow, explorer);
		assertTrue(scn.HasKeyword(explorer, Keyword.ARCHER));
		assertEquals(5, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		// The other [raider] Man, who does not bear it, gains neither.
		assertFalse(scn.HasKeyword(infantry, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(infantry, Keyword.AMBUSH));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
