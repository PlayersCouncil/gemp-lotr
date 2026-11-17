package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
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

public class Card_13_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banners", "13_61");
					put("banners2", "13_61");
					put("faramir", "12_47");    // Faramir, Dúnadan of Gondor: [gondor] Man companion, strength 7; a skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, no text but Knight
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("eowyn", "11_146");     // Éowyn, Shieldmaiden of Rohan: a [rohan] Man companion, strength 6
					// [gondor] cards of five kinds for the discard pile
					put("knight2", "5_35");     // a companion
					put("shield", "1_107");     // Great Shield: a possession
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an artifact
					put("foes", "1_105");       // Foes of Mordor: a condition
					put("dagger", "1_102");     // Dagger Strike: an event
					// A card of another culture for the discard pile
					put("guard", "1_7");        // Dwarf Guard
					// A [gondor] card of a title only the Shadow player's discard pile holds
					put("mail", "1_101");       // Coat of Mail
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BannersBlowingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Banners Blowing
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [gondor] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Make your [gondor] Man strength +3.
		*/

		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");

		assertEquals("Banners Blowing", banners.getBlueprint().getTitle());
		assertNull(banners.getBlueprint().getSubtitle());
		assertFalse(banners.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, banners.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, banners.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, banners.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(banners, Timeword.SKIRMISH));
		assertEquals(2, banners.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingFourOtherGondorCardsItMakesAGondorManStrengthPlusThreeAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");
		var banners2 = scn.GetFreepsCard("banners2");
		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		var knight2 = scn.GetFreepsCard("knight2");
		var shield = scn.GetFreepsCard("shield");
		var narsil = scn.GetFreepsCard("narsil");
		var foes = scn.GetFreepsCard("foes");
		var dagger = scn.GetFreepsCard("dagger");
		var guard = scn.GetFreepsCard("guard");
		var mail = scn.GetShadowCard("mail");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(faramir, knight, pippin, eowyn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToDiscard(banners, knight2, shield, narsil, foes, dagger, guard);
		scn.MoveCardsToDiscard(mail);
		scn.MoveCardsToHand(banners2);

		scn.StartGame();
		assertInZone(Zone.DISCARD, banners, knight2, shield, narsil, foes, dagger, guard, mail);
		assertInZone(Zone.HAND, banners2);
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, runner1},
				new PhysicalCardImpl[]{knight, runner2});
		scn.FreepsResolveSkirmish(faramir);

		assertInZone(Zone.DISCARD, banners, knight2, shield, narsil, foes, dagger, guard, mail);
		assertInZone(Zone.HAND, banners2);
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(banners));
		scn.FreepsUseCardAction(banners);
		// Five other [gondor] cards, of five kinds, for the four.  Not the event itself, a card of another culture,
		// or the [gondor] card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight2, shield, narsil, foes, dagger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(banners, guard, mail));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(knight2, shield, narsil, foes);
		assertInZone(Zone.REMOVED, knight2, shield, narsil, foes);
		assertInZone(Zone.DISCARD, dagger, guard, mail);

		// Your [gondor] Man: Faramir in this skirmish, the Knight assigned and waiting, the Merchant an ally.  Not
		// Pippin, a [gondor] Hobbit, Éowyn, a [rohan] Man, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(faramir, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, eowyn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(faramir);
		assertEquals(10, scn.GetStrength(faramir));
		// The copy played is removed from the game; the copy in hand stays there.
		assertInZone(Zone.REMOVED, banners);
		assertInZone(Zone.HAND, banners2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: Faramir is 7 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(faramir));
	}

	@Test
	public void NotPlayableFromTheDiscardPileWithOnlyThreeOtherGondorCardsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");
		var faramir = scn.GetFreepsCard("faramir");
		var shield = scn.GetFreepsCard("shield");
		var narsil = scn.GetFreepsCard("narsil");
		var foes = scn.GetFreepsCard("foes");
		var dagger = scn.GetFreepsCard("dagger");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(faramir);
		// Four [gondor] cards with the event itself, and a fifth card of another culture.
		scn.MoveCardsToDiscard(banners, shield, narsil, foes, guard);

		scn.StartGame();
		assertInZone(Zone.DISCARD, banners, shield, narsil, foes, guard);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, runner1);
		scn.FreepsResolveSkirmish(faramir);

		assertInZone(Zone.DISCARD, banners, shield, narsil, foes, guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(banners));

		// A fourth other [gondor] card.
		scn.MoveCardsToDiscard(dagger);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(banners));
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndFromHandWithNoGondorManItGoesToTheDiscardPileAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banners = scn.GetFreepsCard("banners");
		var banners2 = scn.GetFreepsCard("banners2");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		var shield = scn.GetFreepsCard("shield");
		var narsil = scn.GetFreepsCard("narsil");
		var foes = scn.GetFreepsCard("foes");
		var dagger = scn.GetFreepsCard("dagger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, eowyn);
		scn.MoveCardsToHand(banners);
		// The second copy lies in the discard pile with four other [gondor] cards.
		scn.MoveCardsToDiscard(banners2, shield, narsil, foes, dagger);

		scn.StartGame();
		assertInZone(Zone.HAND, banners);
		assertInZone(Zone.DISCARD, banners2, shield, narsil, foes, dagger);
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		// A Skirmish event: neither copy at the assignment-phase decision.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(banners));
		assertFalse(scn.FreepsActionAvailable(banners2));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(eowyn, runner1);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(banners));
		assertTrue(scn.FreepsActionAvailable(banners2));
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));
		// No [gondor] Man: the event is played for nothing, and from hand it goes to the discard pile with nothing
		// removed from there.
		scn.FreepsPlayCard(banners);
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, banners, banners2, shield, narsil, foes, dagger);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
