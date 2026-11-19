package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dead", "6_100");
					put("dead2", "6_100");       // a second copy: not unique, and itself a twilight minion once in play
					put("nelya", "2_84");        // Ulaire Nelya: a twilight [wraith] Nazgul; her keywords need 2 burdens or 2 wounds on the Ring-bearer
					put("wisp", "6_108");        // Wisp of Pale Sheen: a twilight [sauron] Wraith; a regroup ability used only on purpose
					put("soldier", "1_271");     // Orc Soldier: a [sauron] Orc who is not twilight; a skirmish ability used only on purpose
					put("cover", "103_64");      // Cover of Darkness: a twilight [wraith] condition, no minion; its ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeadOnesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Dead Ones
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 5
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Twilight</b>. <b>Damage +1</b>.<br>To play this minion, remove a burden or spot a twilight minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dead");

		assertEquals("Dead Ones", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithNoBurdenAndNoTwilightMinionItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var soldier = scn.GetShadowCard("soldier");
		var cover = scn.GetShadowCard("cover");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(soldier);
		scn.MoveCardsToSupportArea(cover);
		scn.SetShadowHand(dead);
		assertInZone(Zone.SUPPORT, cover);
		assertInHand(dead);
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		// An Orc Soldier is no twilight minion, Cover of Darkness is twilight and no minion, and Dead Ones in hand
		// is not in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dead));
	}

	@Test
	public void WithNoBurdenATwilightNazgulIsSpottedAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(nelya);
		scn.SetShadowHand(dead);
		assertInHand(dead);
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(dead));
		scn.ShadowPlayCard(dead);

		assertInZone(Zone.SHADOW_CHARACTERS, dead);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoTwilightMinionABurdenIsRemovedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var soldier = scn.GetShadowCard("soldier");
		var cover = scn.GetShadowCard("cover");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(soldier);
		scn.MoveCardsToSupportArea(cover);
		scn.SetShadowHand(dead);
		assertInZone(Zone.SUPPORT, cover);
		assertInHand(dead);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(dead));
		scn.ShadowPlayCard(dead);

		// Nothing to spot, so the burden is removed with nothing asked: one of two.
		assertInZone(Zone.SHADOW_CHARACTERS, dead);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithABurdenAndASauronTwilightMinionThePlayerIsAskedWhichAndMaySpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var wisp = scn.GetShadowCard("wisp");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(wisp);
		scn.SetShadowHand(dead);
		assertInHand(dead);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		scn.ShadowPlayCard(dead);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Remove a burden"));
		assertTrue(scn.ShadowChoiceAvailable("Spot a twilight minion"));
		scn.ShadowChooseOption("Spot a twilight minion");

		assertInZone(Zone.SHADOW_CHARACTERS, dead);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopyInPlayIsATwilightMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var dead2 = scn.GetShadowCard("dead2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(dead2);
		scn.SetShadowHand(dead);
		assertInZone(Zone.SHADOW_CHARACTERS, dead2);
		assertInHand(dead);
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(dead));
		scn.ShadowPlayCard(dead);

		assertInZone(Zone.SHADOW_CHARACTERS, dead, dead2);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
