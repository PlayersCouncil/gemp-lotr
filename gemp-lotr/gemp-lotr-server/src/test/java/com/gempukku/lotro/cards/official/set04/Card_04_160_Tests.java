package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_160_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mauhur", "4_160");
					// Uruk Seeker: another [isengard] Uruk-hai tracker, strength 6, vitality 2, fierce; its bonus
					// needs a character bearing a search card
					put("seeker", "4_195");
					// Uruk Savage: an [isengard] Uruk-hai who is no tracker; Damage +1 is its whole text
					put("savage", "1_151");
					// Orc Searcher: a [sauron] Orc tracker; its skirmish ability is used only on purpose
					put("searcher", "102_64");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					// used only on purpose
					put("aragorn", "1_89");
					// Ranger's Sword: on Aragorn, strength +2 and damage +1, so that he beats Mauhur (9) without
					// doubling him and places two wounds
					put("rsword", "1_112");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MauhurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Mauhúr, Patrol Leader
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Tracker</b>. <b>Fierce</b>.<br><b>Skirmish:</b> Remove (2) to prevent an [isengard] tracker from taking wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mauhur");

		assertEquals("Mauhúr", card.getBlueprint().getTitle());
		assertEquals("Patrol Leader", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesTwoToSpareTheChosenIsengardTrackerHisWoundsForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mauhur = scn.GetShadowCard("mauhur");
		var seeker = scn.GetShadowCard("seeker");
		var savage = scn.GetShadowCard("savage");
		var searcher = scn.GetShadowCard("searcher");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rsword);

		scn.StartGame();
		scn.MoveMinionsToTable(mauhur, seeker, savage, searcher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mauhur);
		// The other three stay out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(mauhur));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mauhur));

		scn.ShadowUseCardAction(mauhur);
		assertEquals(1, scn.GetTwilight());
		// Mauhur in the skirmish and the Seeker in none are [isengard] trackers; the Savage is no tracker and the
		// Searcher is of [sauron].
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(mauhur, seeker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, searcher));
		scn.ShadowChooseCard(mauhur);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Aragorn won, 10 to 9, with damage +1: neither wound is placed.
		assertEquals(0, scn.GetWoundsOn(mauhur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, mauhur);
		assertTrue(scn.InFierceRound());

		// The fierce skirmish is a skirmish phase of its own.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, mauhur);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(mauhur));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassFierceSkirmishActions();
		// Two wounds on vitality 2
		assertInZone(Zone.DISCARD, mauhur);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithLessThanTwoInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mauhur = scn.GetShadowCard("mauhur");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mauhur);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, mauhur);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mauhur));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(mauhur));
	}
}
