package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_163_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "15_163");
					put("lurtz", "15_162");     // Lurtz, Now Perfected: [uruk-hai] Uruk-hai, damage +1, not fierce; his text answers his winning a skirmish
					put("lurtz11", "11_194");   // Lurtz, Minion of the White Wizard: [uruk-hai] Uruk-hai, archer, damage +1, muster, not fierce; +3 for each exhausted companion (none here)
					put("murderous", "11_195"); // Murderous Uruk: [uruk-hai] Uruk-hai, damage +1 and muster, no other text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, damage +1, no other text
					put("saruman", "12_144");   // Saruman, Agent of the Dark Lord: [uruk-hai] Wizard minion, lurker; his skirmish ability and kill trigger are not reached
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Lurtz's Sword, Mighty Longsword
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be an [uruk-hai] Uruk-hai.<br>Bearer gains <b>hunter 1</b>  <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>.<br>If bearer is Lurtz, he is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Lurtz's Sword", card.getBlueprint().getTitle());
		assertEquals("Mighty Longsword", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnlyOnAnUrukHaiUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var murderous = scn.GetShadowCard("murderous");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, murderous, guard, saruman);
		scn.SetTwilight(5);
		assertInHand(sword);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);

		// Two [uruk-hai] Uruk-hai of different titles; not a [sauron] Uruk-hai, nor an [uruk-hai] minion who is
		// no Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lurtz, murderous));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(murderous);

		assertAttachedTo(sword, murderous);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerGainsHunter1AndIsFierceOnlyIfHeIsLurtz() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var murderous = scn.GetShadowCard("murderous");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, murderous);
		assertFalse(scn.HasKeyword(lurtz, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(murderous, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(murderous, Keyword.FIERCE));

		scn.AttachCardsTo(murderous, sword);

		// Borne by the Murderous Uruk: he gains hunter 1 and is not fierce, being no Lurtz; Lurtz, who does not
		// bear it, gains neither.
		assertAttachedTo(sword, murderous);
		assertEquals(1, scn.GetKeywordCount(murderous, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(murderous, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lurtz, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));

		scn.AttachCardsTo(lurtz, sword);

		// Borne by Lurtz: he gains hunter 1 and is fierce, and the Murderous Uruk has neither now.
		assertAttachedTo(sword, lurtz);
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(murderous, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(murderous, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOtherLurtzBearingItIsFierceToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz11 = scn.GetShadowCard("lurtz11");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz11);
		assertFalse(scn.HasKeyword(lurtz11, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lurtz11, Keyword.HUNTER));

		scn.AttachCardsTo(lurtz11, sword);

		assertAttachedTo(sword, lurtz11);
		assertTrue(scn.HasKeyword(lurtz11, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(lurtz11, Keyword.HUNTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
