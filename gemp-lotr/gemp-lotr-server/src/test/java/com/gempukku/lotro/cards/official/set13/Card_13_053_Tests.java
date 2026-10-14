package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("waste", "13_53");
					put("waste2", "13_53");
					// Sméagol, Poor Creature: a [gollum] Ring-bound companion of no race; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: a [shire] Ring-bound companion; an assignment ability used only on purpose
					put("shiresmeagol", "103_116");
					put("sam", "1_311");         // Sam, Son of Hamfast: a Ring-bound Hobbit; a fellowship ability and a response to Frodo's death
					put("merry", "1_303");       // Merry, From O'er the Brandywine: an unbound Hobbit companion; +2 only with a weapon

					// [gollum] cards of five kinds for the discard pile
					put("slinker", "5_29");      // Sméagol, Slinker: a companion
					put("boat", "13_48");        // Fishing Boat: a possession
					put("nicefish", "15_46");    // Nice Fish: a condition
					put("slimy", "15_50");       // Something Slimy: an event
					put("gollum", "5_24");       // Gollum, Nasty Treacherous Creature: a [gollum] minion, a Shadow card
					// A card of another culture for the discard pile
					put("knight", "5_35");       // Gondorian Knight
					// A [gollum] card of a title that only the Shadow player's discard pile holds
					put("poison", "8_28");       // Spider Poison

					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NakedWasteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Naked Waste
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [gollum] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Spot Sméagol to make a Ring-bound companion strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("waste");

		assertEquals("Naked Waste", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingFourOtherGollumCardsItMakesARingBoundCompanionStrengthPlusThreeAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waste = scn.GetFreepsCard("waste");
		var waste2 = scn.GetFreepsCard("waste2");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var slinker = scn.GetFreepsCard("slinker");
		var boat = scn.GetFreepsCard("boat");
		var nicefish = scn.GetFreepsCard("nicefish");
		var slimy = scn.GetFreepsCard("slimy");
		var gollum = scn.GetFreepsCard("gollum");
		var knight = scn.GetFreepsCard("knight");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol, sam, merry);
		scn.MoveCardsToDiscard(waste, slinker, boat, nicefish, slimy, gollum, knight);
		scn.MoveCardsToDiscard(poison);
		scn.MoveCardsToHand(waste2);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{smeagol, runner1},
				new PhysicalCardImpl[]{frodo, runner2});
		scn.FreepsResolveSkirmish(smeagol);

		assertInZone(Zone.DISCARD, waste, slinker, boat, nicefish, slimy, gollum, knight, poison);
		assertInZone(Zone.HAND, waste2);
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(waste));

		scn.FreepsUseCardAction(waste);
		// Five other [gollum] cards, of five kinds and both sides, for the four. Not the event itself, a card of
		// another culture, or the [gollum] card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slinker, boat, nicefish, slimy, gollum));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(waste, knight, poison));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(slinker, boat, nicefish, gollum);
		assertInZone(Zone.REMOVED, slinker, boat, nicefish, gollum);
		assertInZone(Zone.DISCARD, slimy, knight, poison);

		// A Ring-bound companion: Sméagol (no race, [gollum]) in the skirmish, Frodo (the Ring-bearer) assigned to
		// another, and Sam in no skirmish; not Merry, a Hobbit companion who is not Ring-bound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(smeagol);
		assertEquals(6, scn.GetStrength(smeagol));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		// The copy played is removed from the game; the copy in hand stays there.
		assertInZone(Zone.REMOVED, waste);
		assertInZone(Zone.HAND, waste2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Sméagol, 6, beats his Runner. In the next skirmish, a phase of its own, he is 3 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(smeagol));
	}

	@Test
	public void NotPlayableFromTheDiscardPileWithOnlyThreeOtherGollumCardsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waste = scn.GetFreepsCard("waste");
		var smeagol = scn.GetFreepsCard("smeagol");
		var boat = scn.GetFreepsCard("boat");
		var nicefish = scn.GetFreepsCard("nicefish");
		var slimy = scn.GetFreepsCard("slimy");
		var gollum = scn.GetFreepsCard("gollum");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(smeagol);
		// Four [gollum] cards with the event itself, and a fifth card of another culture.
		scn.MoveCardsToDiscard(waste, boat, nicefish, slimy, knight);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner1);
		scn.FreepsResolveSkirmish(smeagol);

		assertInZone(Zone.DISCARD, waste, boat, nicefish, slimy, knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(waste));

		// A fourth other [gollum] card.
		scn.MoveCardsToDiscard(gollum);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(waste));
	}

	@Test
	public void NotPlayableWithGollumAloneAndFromHandItGoesToTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var waste = scn.GetFreepsCard("waste");
		var waste2 = scn.GetFreepsCard("waste2");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var boat = scn.GetFreepsCard("boat");
		var nicefish = scn.GetFreepsCard("nicefish");
		var slimy = scn.GetFreepsCard("slimy");
		var freepsGollum = scn.GetFreepsCard("gollum");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(waste);
		// The second copy lies in the discard pile with four other [gollum] cards.
		scn.MoveCardsToDiscard(waste2, boat, nicefish, slimy, freepsGollum, knight);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, gollum);
		scn.FreepsResolveSkirmish(frodo);

		// Gollum, the same creature under another title, skirmishes Frodo: neither copy is offered.
		assertInZone(Zone.DISCARD, waste2, boat, nicefish, slimy, freepsGollum, knight);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(waste));
		assertFalse(scn.FreepsActionAvailable(waste2));

		// The [shire] Smeagol is spotted for it.
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(waste));
		assertTrue(scn.FreepsActionAvailable(waste2));

		// Played from hand: nothing is removed from the discard pile, and Frodo, in the skirmish, is chosen.
		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring
		assertEquals(3, scn.GetStrength(shiresmeagol));
		scn.FreepsPlayCard(waste);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, shiresmeagol, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCard(frodo);
		assertEquals(7, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(shiresmeagol));
		assertInZone(Zone.DISCARD, waste, waste2, boat, nicefish, slimy, freepsGollum, knight);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
