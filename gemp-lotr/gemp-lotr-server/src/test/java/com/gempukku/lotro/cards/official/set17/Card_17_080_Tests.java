package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lackey", "17_80");
					put("lackey2", "17_80");

					put("beckoning", "12_83");  // The Beckoning Shadow: [orc] condition, support area; corrupts only at 5 burdens with an empty Free Peoples deck
					put("effort", "15_106");    // Coordinated Effort: [orc] condition, support area; answers only a Free Peoples possession played
					put("greatcost", "12_88");  // Great Cost: [orc] condition, support area; a start-of-skirmish trigger and a Shadow ability, neither reached here
					put("denizen", "11_115");   // Denizen of Khazad-dum: [orc] Orc; +2 only at an underground site
					put("gear", "17_90");       // Rider's Gear: an [orc] possession (armor) that is no condition, for an [orc] Orc
					put("weary", "4_212");      // Weary: an [isengard] condition on a companion; answers only an [isengard] tracker assigned

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, vitality 4; a maneuver ability never used here
					put("gilgalad", "9_15");    // Gil-galad, Elven High King: unbound [elven] Elf; a regroup ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer
					put("barliman", "1_70");    // Barliman Butterbur: an ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishLackeyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Lackey
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Maneuver:</b> Spot 2 [orc] conditions and discard this minion from play to wound an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("lackey");

		assertEquals("Orkish Lackey", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityDiscardsTheLackeyToWoundAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lackey = scn.GetShadowCard("lackey");
		var lackey2 = scn.GetShadowCard("lackey2");
		var beckoning = scn.GetShadowCard("beckoning");
		var effort = scn.GetShadowCard("effort");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, gilgalad, sam);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(lackey, lackey2);
		// Two [orc] conditions of two titles.
		scn.MoveCardsToSupportArea(beckoning, effort);
		// Aragorn exhausted: vitality 4, 3 wounds.
		scn.AddWoundsToChar(aragorn, 3);
		assertInZone(Zone.SUPPORT, beckoning, effort);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gilgalad));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(lackey));
		scn.ShadowUseCardAction(lackey);

		// The cost: this copy goes, the other stays.
		assertInZone(Zone.DISCARD, lackey);
		assertInZone(Zone.SHADOW_CHARACTERS, lackey2);
		// Unbound companions only: not the Ring-bearer, not Sam (Ring-bound), not an ally, not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gilgalad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, barliman, lackey2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// A wound, not an exertion: the exhausted Aragorn is killed.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.GetWoundsOn(gilgalad));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, beckoning, effort);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedUntilTwoOrcConditionsAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lackey = scn.GetShadowCard("lackey");
		var beckoning = scn.GetShadowCard("beckoning");
		var effort = scn.GetShadowCard("effort");
		var greatcost = scn.GetShadowCard("greatcost");
		var denizen = scn.GetShadowCard("denizen");
		var gear = scn.GetShadowCard("gear");
		var weary = scn.GetShadowCard("weary");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(lackey, denizen);
		// One [orc] condition, beside an [orc] possession, a condition of another culture,
		// and an [orc] condition in the discard pile (not in play).
		scn.MoveCardsToSupportArea(beckoning);
		scn.AttachCardsTo(denizen, gear);
		scn.MoveCardsToDiscard(effort);
		assertInZone(Zone.SUPPORT, beckoning);
		assertAttachedTo(gear, denizen);
		assertAttachedTo(weary, aragorn);
		assertInZone(Zone.DISCARD, effort);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lackey));

		// A second [orc] condition: exactly 2.
		scn.MoveCardsToSupportArea(greatcost);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(lackey));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheAbilityIsOfferedAndDiscardsOnlyTheLackey() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lackey = scn.GetShadowCard("lackey");
		var beckoning = scn.GetShadowCard("beckoning");
		var effort = scn.GetShadowCard("effort");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(lackey);
		scn.MoveCardsToSupportArea(beckoning, effort);
		assertInZone(Zone.SUPPORT, beckoning, effort);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(lackey));
		scn.ShadowUseCardAction(lackey);

		// Paid for nothing: both companions are Ring-bound.
		assertInZone(Zone.DISCARD, lackey);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
