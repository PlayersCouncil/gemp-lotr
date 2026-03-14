package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_042_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_42");
					put("southron1", "4_222");   // Desert Warrior - Southron Man, culture(raider)
					put("southron2", "4_222");
					put("orc", "1_271");         // Orc Soldier - not a Raider Man
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskMatriarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Matriarch
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Artifact
		 * Subtype: Support area
		 * Strength: 6
		 * Vitality: 6
		 * Game Text: Each time a Southron is played, you may stack a [raider] Man from play or hand here.
		* 	Maneuver: Remove (6) to make this artifact a <b>fierce</b> mounted Southron minion until the end of the turn that is strength +3 and <b>ambush (1)</b> for each Southron stacked on her. She adds 1 to the Shadow archery total for each Southron stacked on her (limit 6).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Bladetusk Matriarch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(6, card.getBlueprint().getVitality());
	}

	@Test
	public void BladetuskMatriarchStackFilterAcceptsRaiderMan() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata changes stack filter from "Southron" to "culture(raider),Man"
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(card);
		scn.MoveCardsToHand(southron1);
		scn.MoveMinionsToTable(southron2, orc);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		// Play a Southron to trigger the stacking ability
		scn.ShadowPlayCard(southron1);

		// Should trigger optional stacking
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Choose to stack from play
		scn.ShadowChoose("play");

		// Should only offer [raider] Men (southron2), not the orc
		assertEquals(2, scn.ShadowGetCardChoiceCount()); // southron1 (just played) and southron2
		assertFalse(scn.ShadowHasCardChoiceAvailable(orc));
		scn.ShadowChooseCard(southron2);

		assertEquals(1, scn.GetStackedCards(card).size());
		assertInZone(Zone.STACKED, southron2);
	}
}
