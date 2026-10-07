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
					put("card", "2_96");
					put("redbook", "2_113");    // Red Book of Westmarch: a tale in the support area; answers a tale being played
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability that is never used here
					put("pinned", "1_192");     // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					put("pillage", "4_167");    // Pillage of Rohan: Shadow condition, plays on a site you control; idle with no Uruk-hai stacked there
					// Weary: Shadow condition, plays on a companion or ally; answers its bearer being assigned to an
					// [isengard] tracker and nothing else
					put("weary1", "4_212");
					put("weary2", "4_212");
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

		var card = scn.GetFreepsCard("card");

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

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BilboTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void FellowshipAbilityReachesAShadowConditionOnASiteTheOpponentControlsAndNotOneOnAnAllyOrACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("card");
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
}
