package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_008_Tests
{
	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot. Minions laid after a move stand, unassigned, until the regroup move. Followers go back to the support area
	// as a turn ends, so they are attached after a SkipToSite.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eager", "33_8");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability not used
					// [dwarven] followers of two costs
					put("bifur", "30_5");       // Bifur, Inarticulate: cost 1
					put("bombur", "30_7");      // Bombur, Chief Cook: cost 3
					put("beorn", "31_13");      // Beorn, Skin-Changer: a [gandalf] follower, cost 3
					put("pot", "33_5");         // Dwarven Cooking Pot: a [dwarven] possession, support area, cost 1

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; its Assignment ability not used
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, fierce; its Skirmish ability not used
					put("footman", "30_42");    // Goblin Footman: [moria] Orc; its play trigger is not reached by a cheat
					put("bats", "32_30");       // Dark Bats: [moria] Creature; its Shadow ability not used
					put("smaug", "30_66");      // Smaug, The Golden: discarded if not at a mountain; his Regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void EagerToBrawlStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Eager To Brawl
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the fellowship moves during the regroup phase, spot a [dwarven] follower with a twilight cost of X and add (X) to discard up to X minions (except Smaug).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eager");

		assertEquals("Eager To Brawl", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARegroupMoveSpotsBomburToAdd3AndDiscardUpTo3MinionsButNotSmaug() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eager = scn.GetFreepsCard("eager");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var bombur = scn.GetFreepsCard("bombur");
		var beorn = scn.GetFreepsCard("beorn");
		var pot = scn.GetFreepsCard("pot");
		var aggressor = scn.GetShadowCard("aggressor");
		var spider = scn.GetShadowCard("spider");
		var footman = scn.GetShadowCard("footman");
		var bats = scn.GetShadowCard("bats");
		var smaug = scn.GetShadowCard("smaug");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		// Erebor, site 7, a mountain: Smaug stays in play there.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor, spider, footman, bats, smaug);
		scn.AttachCardsTo(balin, bifur);
		scn.MoveCardsToSupportArea(bombur, beorn, pot);
		scn.MoveCardsToHand(eager);

		scn.SkipToMovementDecision();
		assertAttachedTo(bifur, balin);
		assertInZone(Zone.SUPPORT, bombur, beorn, pot);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, spider, footman, bats, smaug);
		assertInHand(eager);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eager));
		scn.FreepsChooseOptionalTrigger(eager);

		// The [dwarven] followers, borne and in the support area; not the [gandalf] follower nor the [dwarven]
		// possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, bombur));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(beorn, pot));
		scn.FreepsChooseCard(bombur);

		// 1 to play + 3 for Bombur's cost, before the move adds its own.
		assertEquals(pool + 1 + 3, scn.GetTwilight());
		// Minions of three cultures and three races; not Smaug, nor Balin (a companion).
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, spider, footman, bats));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smaug, balin));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aggressor, spider, footman);

		assertInDiscard(aggressor, spider, footman, eager);
		assertInZone(Zone.SHADOW_CHARACTERS, bats, smaug);
		assertInZone(Zone.SUPPORT, bombur, beorn, pot);
		assertAttachedTo(bifur, balin);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingBifurAdds1AndDiscardsAtMost1Minion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eager = scn.GetFreepsCard("eager");
		var bifur = scn.GetFreepsCard("bifur");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToHand(eager);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur);
		assertInHand(eager);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor, bats);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, bats);
		assertInHand(eager);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eager));
		scn.FreepsChooseOptionalTrigger(eager);

		// Bifur, the one [dwarven] follower, is spotted unasked: 1 to play + 1 for his cost.
		assertEquals(pool + 1 + 1, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, bats));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(bats);

		assertInDiscard(bats, eager);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheMoveAtTheEndOfTheFellowshipPhaseDoesNotOfferIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eager = scn.GetFreepsCard("eager");
		var bombur = scn.GetFreepsCard("bombur");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(bombur);
		scn.MoveCardsToHand(eager);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		assertInZone(Zone.SUPPORT, bombur);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInHand(eager);

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eager));
		assertInHand(eager);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARegroupMoveDoesNotOfferItWithNoDwarvenFollowerToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eager = scn.GetFreepsCard("eager");
		var beorn = scn.GetFreepsCard("beorn");
		var pot = scn.GetFreepsCard("pot");
		var aggressor = scn.GetShadowCard("aggressor");
		// A [gandalf] follower and a [dwarven] card that is no follower.
		scn.MoveCardsToSupportArea(beorn, pot);
		scn.MoveCardsToHand(eager);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, beorn, pot);
		assertInHand(eager);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToMovementDecision();
		assertInZone(Zone.SUPPORT, beorn, pot);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInHand(eager);

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eager));
		assertInHand(eager);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
