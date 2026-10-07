package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_124_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, who bears The Ruling Ring. Every card named here is in both
	// players' decks.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man
					put("gimli", "1_13");          // Gimli, Son of Gloin: a Dwarf, to wear the armor
					put("bilbo", "1_284");         // Bilbo, Retired Adventurer: an ally

					// Free Peoples cards that companions bear
					put("sting", "1_313");         // Sting: possession; bearer must be Frodo
					put("anduril", "7_80");        // Anduril, King's Blade: artifact; bearer must be Aragorn
					put("athelas", "1_94");        // Athelas: possession; bearer must be a [gondor] Man
					put("alliance", "1_49");       // The Last Alliance of Elves and Men: condition on a [gondor] Man
					put("army", "18_47");          // Elendil's Army: a follower, put on a companion by the rig
					put("armor", "1_8");           // Dwarven Armor: possession; bearer must be a Dwarf
					put("flame", "7_79");          // Anduril, Flame of the West: artifact; Aragorn is vitality +1

					// Free Peoples cards that no companion bears
					put("elvenBow", "1_41");       // Elven Bow: possession, put on the ally by the rig
					put("oldToby", "1_305");       // Old Toby: possession in the support area
					put("sapling", "9_35");        // Sapling of the White Tree: artifact in the support area
					put("promise", "2_112");       // A Promise: condition in the support area

					// A Shadow card that a companion bears. It does nothing unless its bearer is assigned to a
					// skirmish.
					put("poison", "8_28");         // Spider Poison: possession
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "103_124");       // Gorgoroth Wastes
					put("site9", "7_360");         // Dagorlad: Shadow number 9; adds 3 threats when the fellowship moves there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothWastesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gorgoroth Wastes
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8
		 * Shadow Number: 5
		 * Direction: Left
		 * Game Text: When moving here, for each Free Peoples card borne by companions, the Free Peoples player must
		 * 	discard it or add (1).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Gorgoroth Wastes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void OnMovingHereTheFreePeoplesPlayerDiscardsOrPaysOneForEachFreePeoplesCardHisCompanionsBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sting = scn.GetFreepsCard("sting");
		var anduril = scn.GetFreepsCard("anduril");
		var athelas = scn.GetFreepsCard("athelas");
		var alliance = scn.GetFreepsCard("alliance");
		var army = scn.GetFreepsCard("army");
		var elvenBow = scn.GetFreepsCard("elvenBow");
		var oldToby = scn.GetFreepsCard("oldToby");
		var sapling = scn.GetFreepsCard("sapling");
		var promise = scn.GetFreepsCard("promise");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		// Five Free Peoples cards on two companions: a possession on the Ring-bearer; an artifact, a
		// possession (hindered), a condition and a follower on Aragorn.
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(aragorn, anduril, athelas, alliance, army);
		scn.HinderCard(athelas);
		// Beside them: the Shadow player's possession on the Ring-bearer, a Free Peoples possession on an
		// ally, and a Free Peoples possession, artifact and condition in the support area. The One Ring is
		// on the Ring-bearer as always.
		scn.AttachCardsTo(frodo, poison);
		scn.AttachCardsTo(bilbo, elvenBow);
		scn.MoveCardsToSupportArea(oldToby, sapling, promise);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals("Gorgoroth Wastes", scn.GetCurrentSite().getBlueprint().getTitle());

		assertTrue(scn.FreepsHasCardChoiceAvailable(sting, anduril, athelas, alliance, army));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, poison, elvenBow, oldToby, sapling, promise));
		assertEquals(5, scn.FreepsGetCardChoiceCount());
		// Any of them may be discarded, from none to all.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(5, scn.FreepsGetChoiceMax());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChooseCards(sting, army);
		assertInDiscard(sting, army);
		assertAttachedTo(anduril, aragorn);
		assertAttachedTo(athelas, aragorn);
		assertTrue(scn.IsHindered(athelas));
		assertAttachedTo(alliance, aragorn);
		// 2 companions + 5 for the site + 1 each for Anduril, the hindered Athelas and the Last Alliance
		assertEquals(10, scn.GetTwilight());

		assertAttachedTo(ring, frodo);
		assertAttachedTo(poison, frodo);
		assertAttachedTo(elvenBow, bilbo);
		assertInZone(Zone.SUPPORT, oldToby, sapling, promise);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardingEveryCardAHinderedOneIncludedAddsNothingBeyondTheMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var athelas = scn.GetFreepsCard("athelas");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(aragorn, athelas);
		scn.HinderCard(athelas);

		scn.FreepsPass();
		assertEquals("Gorgoroth Wastes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sting, athelas));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCards(sting, athelas);
		assertInDiscard(sting, athelas);
		// 2 companions + 5 for the site
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhoeverMovesHereIsTheOneAskedAndKeepingEveryCardAddsOneForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstFrodo = scn.GetRingBearer();
		var firstSting = scn.GetFreepsCard("sting");
		var secondFrodo = scn.GetShadowRingBearer();
		var secondAragorn = scn.GetShadowCard("aragorn");
		var secondSting = scn.GetShadowCard("sting");
		var secondAthelas = scn.GetShadowCard("athelas");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AttachCardsTo(firstFrodo, firstSting);

		// The first player's Frodo moves here alone, bearing Sting. The first player is asked, and keeps it.
		scn.FreepsPass();
		assertEquals("Gorgoroth Wastes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsHasCardChoiceAvailable(firstSting));
		assertEquals(1, scn.FreepsGetCardChoiceCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsDeclineChoosing();
		assertAttachedTo(firstSting, firstFrodo);
		// 1 companion + 5 for the site + 1 for Sting
		assertEquals(7, scn.GetTwilight());
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship, still at site 7, moves here in its turn: his Frodo with his own
		// Sting, his Aragorn with his own Athelas. He is the one asked, and about his own two cards only.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveCompanionsToTable(secondAragorn);
		scn.AttachCardsTo(secondFrodo, secondSting);
		scn.AttachCardsTo(secondAragorn, secondAthelas);
		assertEquals(0, scn.GetTwilight());

		scn.ShadowPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowHasCardChoiceAvailable(secondSting, secondAthelas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(firstSting));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		scn.ShadowDeclineChoosing();
		assertAttachedTo(secondSting, secondFrodo);
		assertAttachedTo(secondAthelas, secondAragorn);
		assertAttachedTo(firstSting, firstFrodo);
		// 2 companions + 5 for the site + 1 each for the two cards kept
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void MovingOnFromHereAsksNothingAndAddsNothingForTheCardsStillBorne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.AttachCardsTo(frodo, sting);

		scn.FreepsPass();
		assertEquals("Gorgoroth Wastes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sting));
		scn.FreepsDeclineChoosing();
		// 1 companion + 5 for the site + 1 for Sting
		assertEquals(7, scn.GetTwilight());

		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertAttachedTo(sting, frodo);
		// 7 from before + 1 companion + 9 for Dagorlad
		assertEquals(17, scn.GetTwilight());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesCardBorneByACompanionNothingIsAskedOrAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var bilbo = scn.GetFreepsCard("bilbo");
		var elvenBow = scn.GetFreepsCard("elvenBow");
		var oldToby = scn.GetFreepsCard("oldToby");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SkipToSite(7);
		// The Ring-bearer bears The One Ring, which is of no side, and the Shadow player's possession; a
		// Free Peoples possession is on an ally and another in the support area.
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(frodo, poison);
		scn.AttachCardsTo(bilbo, elvenBow);
		scn.MoveCardsToSupportArea(oldToby);

		scn.FreepsPass();
		assertEquals("Gorgoroth Wastes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 1 companion + 5 for the site
		assertEquals(6, scn.GetTwilight());
		assertAttachedTo(ring, frodo);
		assertAttachedTo(poison, frodo);
		assertAttachedTo(elvenBow, bilbo);
		assertInZone(Zone.SUPPORT, oldToby);
	}

	@Test
	public void ACardThatLeavesPlayWithItsDyingBearerIsNotPaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var flame = scn.GetFreepsCard("flame");
		var athelas = scn.GetFreepsCard("athelas");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, flame, athelas);
		// Vitality 4 + 1 for Anduril, and 4 wounds: without Anduril he dies.
		scn.AddWoundsToChar(aragorn, 4);
		assertEquals(1, scn.GetVitality(aragorn));

		scn.FreepsPass();
		assertEquals("Gorgoroth Wastes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsHasCardChoiceAvailable(flame, athelas));

		// Only Anduril is chosen. Aragorn dies of losing it and his Athelas goes with him: it was not kept,
		// so nothing is added for it.
		scn.FreepsChooseCards(flame);
		assertInDiscard(flame, athelas);
		assertInZone(Zone.DEAD, aragorn);
		// 1 companion (Aragorn is dead before the move's twilight is added) + 5 for the site
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardBorneByAHinderedCompanionIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var sting = scn.GetFreepsCard("sting");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(gimli, armor);
		// A hindered card has no card type: Gimli is not "a companion" while he is hindered.
		scn.HinderCard(gimli);

		scn.FreepsPass();
		assertEquals("Gorgoroth Wastes", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sting));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor));
		assertEquals(1, scn.FreepsGetCardChoiceCount());

		scn.FreepsDeclineChoosing();
		assertAttachedTo(sting, frodo);
		assertAttachedTo(armor, gimli);
		// 1 for Frodo (a hindered companion adds nothing to a move) + 5 for the site + 1 for Sting
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
