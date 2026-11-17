
package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
                new HashMap<>() {{
                    put("asfaloth", "1_31");
                    put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, strength 6; a response used only on purpose
                    put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, strength 6; an archery ability only
                    put("erestor", "3_14");     // Erestor, Chief Advisor to Elrond: Elf ally, home site 3, strength 5; to play, spot an Elf
                    put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, home site 6, strength 3; an archery ability only
                    put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability only
                    put("bow", "1_41");         // Elven Bow: a possession an Elf bears, cost 1, no strength; bearer is an archer
                }},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Arwen the ally stands on a board of her own: she and Arwen the companion share a unique title.
	protected VirtualTableScenario GetArwenTheAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("asfaloth", "1_31");
					// Arwen, Lady of Rivendell: Elf ally, home site 3; her text is about Aragorn, who is not here
					put("arwen", "102_6");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rohirrim Road as site 1: "Fellowship: Exert an Elf to make the twilight cost of each condition and
	// possession +2 until the end of the turn."
	protected VirtualTableScenario GetRohirrimRoadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("asfaloth", "1_31");
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, vitality 3
					put("bow", "1_41");         // Elven Bow: a possession an Elf bears, cost 1
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.KingSites);
					put("site1", "7_332");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AsfalothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Asfaloth
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +2
		 * Game Text: Bearer must be an Elf. When played on Arwen, Asfaloth's twilight cost is -2.
		 * While at a plains site, bearer is strength +2. Discard Asfaloth when at an underground site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("asfaloth");

		assertEquals("Asfaloth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		// A Man and a Hobbit: companions, and neither an Elf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(asfaloth, arwen, orophin);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(asfaloth));

		// An Elf who is an ally
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.FreepsPlayAvailable(asfaloth));

		// An Elf who is a companion
		scn.FreepsPlayCard(arwen);

		scn.FreepsPlayCard(asfaloth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(orophin);
		assertAttachedTo(asfaloth, orophin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwilightCost2OnNonArwen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(5, scn.GetStrength(erestor));
		scn.FreepsPlayCard(asfaloth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, erestor, orophin));

		// Arwen is on the table, but she is not the one it is played on.
		scn.FreepsChooseCard(erestor);
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(asfaloth, erestor);
		assertEquals(7, scn.GetStrength(erestor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwilightCost0OnArwen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, erestor, orophin));

		scn.FreepsChooseCard(arwen);
		assertEquals(0, scn.GetTwilight());
		assertAttachedTo(asfaloth, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnArwenWithItsCostRaisedByTwoItCostsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohirrimRoadScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToHand(asfaloth, bow);

		scn.StartGame();

		// A cost of 2 less 2 reads 0 whatever the discount's size above 2. Rohirrim Road makes each possession
		// cost 2 more for the turn; Legolas is the only one of the two Elves it is asked of here by choice.
		var road = scn.GetCurrentSite();
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(road);
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetTwilight());

		// The Elven Bow shows the 2 more: 1 + 2. Legolas is the only Elf it is offered: it is played on him.
		scn.FreepsPlayCard(bow);
		scn.FreepsChooseCard(legolas);
		assertAttachedTo(bow, legolas);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertAttachedTo(asfaloth, arwen);
		// 3 + (2 for Asfaloth + 2 for the site - 2 on Arwen)
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnArwenTheAllyItCostsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArwenTheAllyScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveAlliesToTable(arwen);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, legolas));

		scn.FreepsChooseCard(arwen);
		assertAttachedTo(asfaloth, arwen);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus2AtPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		// Bag End is no plains: the 2 printed on Asfaloth and no more.
		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(legolas));

		// To Ettenmoors, a plains site. Legolas, an Elf with no mount, is there too.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(10, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(legolas));
		assertAttachedTo(asfaloth, arwen);

		// On to Rivendell Valley, which is neither plains nor underground.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(8, scn.GetStrength(arwen));
		assertAttachedTo(asfaloth, arwen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SelfDiscardsAtUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		// Another possession beside it on the same Elf.
		scn.AttachCardsTo(arwen, asfaloth, bow);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(asfaloth, arwen);
		assertEquals(8, scn.GetStrength(arwen));

		// To Balin's Tomb, which is underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertInDiscard(asfaloth);
		assertAttachedTo(bow, arwen);
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtAnUndergroundSiteItIsDiscardedAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(asfaloth);
		scn.FreepsRefreshDecision();

		// Balin's Tomb is underground: being there is a state, so Asfaloth is discarded as it comes into play.
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsPlayAvailable(asfaloth));
		scn.FreepsPlayCard(asfaloth);
		assertInDiscard(asfaloth);
		assertEquals(0, scn.GetAttachedCards(legolas).size());
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnAllyItIsAtHisHomeSiteAndStaysWhenTheFellowshipGoesUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(orophin, asfaloth);
		assertAttachedTo(asfaloth, orophin);

		// An ally is at his home site, not where the fellowship is: Orophin's is site 6, Galadriel's Glade, and
		// the fellowship's move to Balin's Tomb takes neither him nor Asfaloth underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertAttachedTo(asfaloth, orophin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByAHinderedCompanionItIsDiscardedAtAnUndergroundSiteAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(arwen, asfaloth, bow);
		// A hindered Arwen is no companion to be found, and is with the fellowship all the same: what keeps
		// Asfaloth out of Moria is being borne by an ally, who is at his home site, and nothing else (Ketura
		// on Q-0252).
		scn.HinderCard(arwen);
		assertTrue(scn.IsHindered(arwen));
		assertFalse(scn.IsHindered(asfaloth));
		assertAttachedTo(asfaloth, arwen);

		// To Balin's Tomb, which is underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertTrue(scn.IsHindered(arwen));
		assertInDiscard(asfaloth);
		assertAttachedTo(bow, arwen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByAnAllyItIsAtHisHomeSiteAndGivesNoMoreWhenTheFellowshipIsAtAPlainsSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.AttachCardsTo(orophin, asfaloth);

		// Orophin's 3 + the 2 printed on Asfaloth
		assertEquals(5, scn.GetStrength(orophin));

		// The fellowship moves to Ettenmoors, a plains site. Orophin is at his home, site 6, and no site 6 of
		// the Fellowship block is a plains.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(5, scn.GetStrength(orophin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TransferBetweenAllyAndCompanionPaysTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// Bug reported: https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=FallsLover$ilpahhdbsdmp8i2x
		// Site 6 permitted multiple transfers between Rumil and Legolas on site 6 with 0 twilight added to the pool.

		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveAlliesToTable(erestor, orophin);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(erestor);
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(asfaloth, erestor);

		// Erestor is at his home, site 3; the companions are at site 1 and Orophin at site 6: nobody is with him.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsTransferAvailable(asfaloth));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(3);

		// At Erestor's home site the companions are where he is.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(asfaloth));
		scn.FreepsTransferCard(asfaloth);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin));

		scn.FreepsChooseCard(legolas);
		assertAttachedTo(asfaloth, legolas);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TransferredToArwenItCostsItsPrintedTwoAndNotTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.AttachCardsTo(legolas, asfaloth);

		scn.StartGame();

		// "When played on Arwen, Asfaloth's twilight cost is -2": a transfer pays the twilight cost again and is
		// not a play (CR 4.2 "transfer"), so moving it to Arwen costs the printed 2.  She is the only other Elf.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(asfaloth));
		scn.FreepsTransferCard(asfaloth);
		assertAttachedTo(asfaloth, arwen);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
