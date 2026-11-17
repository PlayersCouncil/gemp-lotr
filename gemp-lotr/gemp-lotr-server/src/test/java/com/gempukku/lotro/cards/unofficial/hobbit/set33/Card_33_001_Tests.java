package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_001_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Site N is reached by
	// SkipToSite(N - 1) and the fellowship phase's move; the Forges are laid after the skip, since the turns it plays
	// end in regroup.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forges", "33_1");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7; a Maneuver ability not used
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally; his regroup offer wants a follower

					// [dwarven] fortifications, a fortification of another culture and a [dwarven] possession that is none.
					put("barricade", "31_4");   // Great Barricade: [dwarven] fortification; its regroup-move discard not reached
					put("horn", "33_9");        // Horn of Erebor: [dwarven] fortification; its +2 wants a follower, none in play
					put("stronghold", "102_48");// Stronghold of My Fathers: [rohan] fortification possession; a Response for [rohan] companions
					put("pot", "33_5");         // Dwarven Cooking Pot: [dwarven] support-area possession; its texts not reached

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; its Assignment ability not used
					put("goblin", "32_27");     // Assaulting Goblin: [moria] Orc, strength 7, vitality 2; a response at a battleground
					put("tomnoddy", "31_53");   // Old Tomnoddy: [spider] Spider, vitality 3; a text on its own play only
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheAncientForgesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: The Ancient Forges
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. To play, exert a [dwarven] companion.<br>At sites 7 and 8, Balin is
		 * 	strength +2.<br><b>Skirmish:</b> At site 8, exert Balin and discard a [dwarven] fortification to wound a
		 * 	minion twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forges");

		assertEquals("The Ancient Forges", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsADwarvenCompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.MoveCardsToHand(forges);
		scn.FreepsRefreshDecision();
		assertInHand(forges);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));

		assertTrue(scn.FreepsPlayAvailable(forges));
		scn.FreepsPlayCard(forges);
		// Two [dwarven] companions; not Bilbo, a [shire] companion, nor Dain, a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(nori);

		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertInZone(Zone.SUPPORT, forges);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarvenCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.MoveCardsToHand(forges);
		// Balin, vitality 3, exhausted; Bilbo and Dain could exert, and are no [dwarven] companion.
		scn.AddWoundsToChar(balin, 2);
		scn.FreepsRefreshDecision();
		assertInHand(forges);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertEquals(0, scn.GetWoundsOn(dain));

		assertFalse(scn.FreepsPlayAvailable(forges));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSites7And8BalinIsStrengthPlus2AndAnotherDwarfIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(forges);
		assertInZone(Zone.SUPPORT, forges);
		assertEquals(7, scn.GetStrength(balin));

		// Erebor, site 7.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(nori));

		// Smaug's Den, site 8, by the regroup move.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(9, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(nori));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite6BalinKeepsHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(forges);
		assertInZone(Zone.SUPPORT, forges);
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite9BalinKeepsHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(forges);
		assertInZone(Zone.SUPPORT, forges);
		assertEquals(9, scn.GetStrength(balin));
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite8SkirmishAbilityExertsBalinAndDiscardsADwarvenFortificationToWoundAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var barricade = scn.GetFreepsCard("barricade");
		var horn = scn.GetFreepsCard("horn");
		var stronghold = scn.GetFreepsCard("stronghold");
		var pot = scn.GetFreepsCard("pot");
		var aggressor = scn.GetShadowCard("aggressor");
		var goblin = scn.GetShadowCard("goblin");
		var tomnoddy = scn.GetShadowCard("tomnoddy");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(forges, barricade, horn, stronghold, pot);
		assertInZone(Zone.SUPPORT, forges, barricade, horn, stronghold, pot);
		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor, goblin, tomnoddy);
		// The Goblin, vitality 2, exhausted.
		scn.AddWoundsToChar(goblin, 1);
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(tomnoddy));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, aggressor}, new PhysicalCardImpl[]{nori, goblin});
		// Old Tomnoddy left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));

		assertTrue(scn.FreepsActionAvailable(forges));
		scn.FreepsUseCardAction(forges);
		// Balin exerts, not Nori.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		// The [dwarven] fortifications, the Forges among them; not the [rohan] one, nor the Pot, no fortification.
		assertTrue(scn.FreepsHasCardChoiceAvailable(forges, barricade, horn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stronghold, pot));
		scn.FreepsChooseCard(barricade);
		assertInDiscard(barricade);
		assertInZone(Zone.SUPPORT, forges, horn, stronghold, pot);

		// Any minion: the one Balin skirmishes, one waiting for Nori (exhausted) and one unassigned.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, goblin, tomnoddy));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(tomnoddy);
		assertEquals(2, scn.GetWoundsOn(tomnoddy));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileBalinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var barricade = scn.GetFreepsCard("barricade");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(forges, barricade);
		assertInZone(Zone.SUPPORT, forges, barricade);
		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor);
		// Balin, vitality 3, exhausted; Nori could exert, and is no Balin.
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(forges));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedAtSite7() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		var barricade = scn.GetFreepsCard("barricade");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(forges, barricade);
		assertInZone(Zone.SUPPORT, forges, barricade);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor);
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(forges));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedAtSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetFreepsCard("forges");
		var balin = scn.GetFreepsCard("balin");
		var barricade = scn.GetFreepsCard("barricade");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(forges, barricade);
		assertInZone(Zone.SUPPORT, forges, barricade);
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(aggressor);
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(forges));
	}
}
