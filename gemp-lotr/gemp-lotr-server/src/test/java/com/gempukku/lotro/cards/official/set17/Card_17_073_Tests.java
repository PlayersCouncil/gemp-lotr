package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "17_73");
					put("denizen", "11_115");   // Denizen of Khazad-dûm: [orc] Orc; strength +2 only at an underground site
					put("halftroll", "14_12");  // Half-troll of Far Harad: an [orc] minion who is no Orc; its trigger wants more companions than minions
					put("brute", "10_90");      // Mordor Brute: a [sauron] Orc; a wound bar for Orcs bearing possessions
					put("shadow", "12_83");     // The Beckoning Shadow: an [orc] condition, no minion; nothing below 5 burdens
					put("gilgalad", "9_15");    // Gil-galad, Elven High King: companion, strength 9, vitality 4; a regroup ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Berserker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 17
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When this minion wins a skirmish, discard an [orc] minion from play.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("berserker");

		assertEquals("Orkish Berserker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningItDiscardsAnOrcMinionOfTheShadowPlayersChoiceItselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		var shadow = scn.GetShadowCard("shadow");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gilgalad, aragorn);
		scn.MoveCardsToSupportArea(shadow);
		assertInZone(Zone.SUPPORT, shadow);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, denizen, halftroll, brute);
		scn.SkipToAssignments();
		// The Denizen's skirmish is still to come; the Half-troll and the Brute are in none.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gilgalad, berserker}, new PhysicalCardImpl[]{aragorn, denizen});
		scn.ShadowDeclineAssignments();

		// 17 against 9: a win, and not an overwhelm.
		scn.FreepsResolveSkirmish(gilgalad);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetWoundsOn(gilgalad));
		assertTrue(scn.ShadowHasCardChoiceAvailable(berserker, denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brute, shadow));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertInZone(Zone.DISCARD, denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, brute);
		assertInZone(Zone.SUPPORT, shadow);
		// Aragorn's skirmish lost its only minion, so no skirmish is left to resolve.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishItLivesThroughDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gilgalad, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, denizen, halftroll);
		// 9 + 10 = 19 beats 17, short of the 34 that would overwhelm him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gilgalad, null, 10));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gilgalad, berserker}, new PhysicalCardImpl[]{aragorn, denizen});
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(berserker));

		scn.FreepsResolveSkirmish(gilgalad);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(berserker));
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
