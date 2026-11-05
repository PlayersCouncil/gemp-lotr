package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_009_Tests
{
	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot. Followers go back to the support area as a turn ends, so they are attached after a SkipToSite.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "33_9");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7; a Maneuver ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, strength 7; a Maneuver ability not used
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion, strength 6; a Skirmish ability not used

					// [dwarven] followers: each gives its bearer its printed strength
					put("bifur", "30_5");       // Bifur, Inarticulate: cost 1, strength +1
					put("bombur", "30_7");      // Bombur, Chief Cook: cost 3, strength +2, damage +1
					put("dori", "30_8");        // Dori, Really a decent fellow: cost 1, strength +1
					put("gwaihir", "31_15");    // Gwaihir, Lord of the Eagles: a [gandalf] follower, strength +1

					// [dwarven] cards that are no follower, and fortifications
					put("crown", "32_1");       // Crown of Erebor: [dwarven] artifact, support area; its texts not reached
					put("pot", "33_5");         // Dwarven Cooking Pot: [dwarven] possession, support area, no fortification
					put("barricade", "31_4");   // Great Barricade: [dwarven] fortification possession, support area
					put("fortress", "54_276");  // Fortress Never Fallen: a [rohan] fortification condition, support area

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc; its Assignment ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HornofEreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Horn of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. To play, spot 2 [dwarven] followers.
		 * 	At sites 7 and 8, each [dwarven] companion bearing a [dwarven] follower is strength +2.
		 * 	<b>Fellowship:</b> Discard a [dwarven] fortification to draw 2 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Horn of Erebor", card.getBlueprint().getTitle());
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
	public void CanBePlayedOnlyOnceTwoDwarvenFollowersAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var bifur = scn.GetFreepsCard("bifur");
		var dori = scn.GetFreepsCard("dori");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var crown = scn.GetFreepsCard("crown");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(gwaihir, crown);

		scn.StartGame();
		scn.AttachCardsTo(balin, dori);
		scn.MoveCardsToHand(horn, bifur);
		scn.FreepsRefreshDecision();
		assertAttachedTo(dori, balin);
		assertInZone(Zone.SUPPORT, gwaihir, crown);
		assertInHand(horn, bifur);

		// One [dwarven] follower, borne by Balin; beside it a [gandalf] follower and a [dwarven] artifact.
		assertFalse(scn.FreepsPlayAvailable(horn));

		// A second [dwarven] follower, played to the support area.
		scn.FreepsPlayCard(bifur);
		assertInZone(Zone.SUPPORT, bifur);
		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);
		assertInZone(Zone.SUPPORT, horn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite7EachDwarvenCompanionBearingADwarvenFollowerIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bifur = scn.GetFreepsCard("bifur");
		var bombur = scn.GetFreepsCard("bombur");
		var dori = scn.GetFreepsCard("dori");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, nori, dwalin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(horn);
		scn.AttachCardsTo(balin, bifur);
		scn.AttachCardsTo(nori, bombur);
		// Bilbo, a [shire] companion, bearing a [dwarven] follower.
		scn.AttachCardsTo(bilbo, dori);
		assertInZone(Zone.SUPPORT, horn);
		assertAttachedTo(bifur, balin);
		assertAttachedTo(bombur, nori);
		assertAttachedTo(dori, bilbo);

		// Esgaroth, site 6: nothing yet. 7 + 1 Bifur; 7 + 2 Bombur; Dwalin bears nothing; 3 + 1 Dori.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(balin));
		assertEquals(9, scn.GetStrength(nori));
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(4, scn.GetStrength(bilbo));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertAttachedTo(bifur, balin);
		assertAttachedTo(bombur, nori);
		assertEquals(10, scn.GetStrength(balin));
		assertEquals(11, scn.GetStrength(nori));
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(4, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite7ADwarvenCompanionBearingAFollowerOfAnotherCultureIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var bifur = scn.GetFreepsCard("bifur");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveCompanionsToTable(balin, gloin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(horn);
		scn.AttachCardsTo(balin, bifur);
		scn.AttachCardsTo(gloin, gwaihir);
		assertAttachedTo(bifur, balin);
		assertAttachedTo(gwaihir, gloin);
		// 7 + 1 Bifur; 6 + 1 Gwaihir.
		assertEquals(8, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(gloin));

		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(gloin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite8TheBonusHoldsAndAtSite9ItIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bifur = scn.GetFreepsCard("bifur");
		scn.MoveCompanionsToTable(balin, dwalin);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToSupportArea(horn);
		scn.AttachCardsTo(balin, bifur);
		assertInZone(Zone.SUPPORT, horn);
		assertAttachedTo(bifur, balin);

		// Smaug's Den, site 8: 7 + 1 Bifur + 2.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(dwalin));

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertAttachedTo(bifur, balin);
		assertEquals(8, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(dwalin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsADwarvenFortificationToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var barricade = scn.GetFreepsCard("barricade");
		var pot = scn.GetFreepsCard("pot");
		var fortress = scn.GetFreepsCard("fortress");
		scn.MoveCardsToSupportArea(horn, barricade, pot, fortress);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, horn, barricade, pot, fortress);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsActionAvailable(horn));
		scn.FreepsUseCardAction(horn);
		// The Horn itself and the Great Barricade; not the Cooking Pot (no fortification) nor the [rohan] Fortress.
		assertTrue(scn.FreepsHasCardChoiceAvailable(horn, barricade));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pot, fortress));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(barricade);

		assertInDiscard(barricade);
		assertInZone(Zone.SUPPORT, horn, pot, fortress);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The Horn is the one [dwarven] fortification left, and pays for itself: two more, four in all this phase.
		assertTrue(scn.FreepsActionAvailable(horn));
		scn.FreepsUseCardAction(horn);
		assertInDiscard(horn);
		assertInZone(Zone.SUPPORT, pot, fortress);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var barricade = scn.GetFreepsCard("barricade");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(horn, barricade);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		assertInZone(Zone.SUPPORT, horn, barricade);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(horn));
		assertInZone(Zone.SUPPORT, horn, barricade);
	}
}
