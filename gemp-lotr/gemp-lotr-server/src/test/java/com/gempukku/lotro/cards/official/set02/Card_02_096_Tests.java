package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bilbo", "2_96");
					put("redbook", "2_113");    // Red Book of Westmarch: a tale in the support area; answers a tale being played
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability that is never used here
					put("pinned", "1_192");     // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					put("pillage", "4_167");    // Pillage of Rohan: Shadow condition, plays on a site you control; idle with no Uruk-hai stacked there
					// Weary: Shadow condition, plays on a companion or ally; answers its bearer being assigned to an
					// [isengard] tracker and nothing else
					put("weary1", "4_212");
					put("weary2", "4_212");
					put("harp", "18_3");        // Thorin's Harp: [dwarven] tale, a condition in the support area; a maneuver ability never used
					put("saga", "1_114");       // The Saga of Elendil: [gondor] tale, a condition on a [gondor] companion; a skirmish ability
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition in the support area that is no tale; no Dwarf in play
					put("trolls", "1_314");     // Stone Trolls: [shire] tale, here only in hand
					put("breath", "1_207");     // Black Breath: [wraith] Shadow condition, support area; a skirmish ability
					put("scaffolding", "6_73"); // Scaffolding: [isengard] Shadow POSSESSION in the support area; idle here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Bilbo, Well-spoken Gentlehobbit
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 3
		 * Site Number: 1
		 * Game Text: <b>Fellowship:</b> Exert Bilbo and discard a tale to discard a Shadow condition from an opponent's support area.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bilbo");

		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Well-spoken Gentlehobbit", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void FellowshipAbilityReachesAShadowConditionOnASiteTheOpponentControlsAndNotOneOnAnAllyOrACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var redbook = scn.GetFreepsCard("redbook");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pinned = scn.GetShadowCard("pinned");
		var pillage = scn.GetShadowCard("pillage");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(redbook, pinned);
		// In the opponent's support area: a condition lying there, and one on a site he controls.
		scn.AttachCardsTo(site1, pillage);
		// Not in it: the opponent's condition on Bilbo, which is in Bilbo's owner's support area, and one on a
		// companion, which is in nobody's.
		scn.AttachCardsTo(bilbo, weary1);
		scn.AttachCardsTo(aragorn, weary2);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInDiscard(redbook);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, pillage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weary1, weary2));
		scn.FreepsChooseCard(pillage);

		assertInDiscard(pillage);
		assertInZone(Zone.SUPPORT, pinned);
		assertAttachedTo(weary1, bilbo);
		assertAttachedTo(weary2, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsBilboAndDiscardsTheChosenTaleToDiscardTheChosenShadowConditionFromTheOpponentsSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var harp = scn.GetFreepsCard("harp");
		var saga = scn.GetFreepsCard("saga");
		var lord = scn.GetFreepsCard("lord");
		var pinned = scn.GetShadowCard("pinned");
		var breath = scn.GetShadowCard("breath");
		var scaffolding = scn.GetShadowCard("scaffolding");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(harp, lord, pinned, breath, scaffolding);
		scn.AttachCardsTo(aragorn, saga);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));
		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));

		// A tale of either culture and either place; not a condition that is no tale.
		assertTrue(scn.FreepsHasCardChoiceAvailable(harp, saga));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lord));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(saga);
		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, harp, lord);

		// A Shadow condition of either culture there; not a Shadow possession beside them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, breath));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scaffolding));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(breath);
		assertInDiscard(breath);
		assertInZone(Zone.SUPPORT, pinned, scaffolding);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileBilboIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var harp = scn.GetFreepsCard("harp");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(harp, pinned);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(bilbo, 2);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bilbo));
		assertInZone(Zone.SUPPORT, harp, pinned);
	}

	@Test
	public void IsNotOfferedWithNoTaleInPlayThoughOneIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var lord = scn.GetFreepsCard("lord");
		var trolls = scn.GetFreepsCard("trolls");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(lord, pinned);
		scn.MoveCardsToHand(trolls);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bilbo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInHand(trolls);
	}
}
