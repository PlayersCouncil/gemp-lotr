package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("living", "12_130");
					// Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4; a fellowship ability and a response to
					// Frodo's death, neither used here
					put("sam", "1_311");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in);
					// a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Bounder: [shire] Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability not used
					put("aragorn", "1_89");
					// Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, vitality 4; his maneuver ability needs a
					// site his player controls and his regroup ability is used only on purpose
					put("ted", "18_75");
					// Concerning Hobbits: [shire] Skirmish event, "For each dwelling and forest site on the adventure
					// path, make a Hobbit strength +1": none of either at sites 1 and 2, so it does nothing more
					put("concerning", "11_161");
					// Hobbit Sword: [shire] hand weapon possession, bearer must be a Hobbit
					put("sword", "1_299");
					// Let Folly Be Our Cloak: [gandalf] Fellowship event, "Exert X companions to remove (X)"
					put("folly", "1_77");
					// Dear Friends: [shire] Fellowship event, "Exert a companion to place a Free Peoples card from your
					// discard pile beneath your draw deck"
					put("friends", "2_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SimpleLivingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Simple Living
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If you play a [shire] event, discard this condition to heal each Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("living");

		assertEquals("Simple Living", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingAShireEventDiscardsItToHealEveryHobbitOnceAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var concerning = scn.GetFreepsCard("concerning");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToSupportArea(living);
		scn.MoveCompanionsToTable(sam, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(concerning);

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(ted, 1);
		assertInZone(Zone.SUPPORT, living);
		assertInHand(concerning);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(ted));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ted);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(concerning);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(living));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, living);
		// One wound off each Hobbit: a [shire] and a [gondor] companion, an ally, and the Shadow player's minion.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		// Aragorn is no Hobbit.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, concerning);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AShireCardThatIsNoEventOrAnEventOfAnotherCultureDoesNotOfferIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var folly = scn.GetFreepsCard("folly");
		scn.MoveCardsToSupportArea(living);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(sword, folly);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, living);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInHand(sword);
		assertInHand(folly);

		// A [shire] possession.
		scn.FreepsPlayCard(sword);
		scn.FreepsChooseCard(sam);
		assertAttachedTo(sword, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(living));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [gandalf] event, Aragorn exerting for it.
		scn.FreepsPlayCard(folly);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(living));
		assertInZone(Zone.SUPPORT, living);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHobbitWoundedItIsStillOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var friends = scn.GetFreepsCard("friends");
		var folly = scn.GetFreepsCard("folly");
		scn.MoveCardsToSupportArea(living);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(friends);
		// A Free Peoples card in the discard pile for Dear Friends to put under the deck.
		scn.MoveCardsToDiscard(folly);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, living);
		assertInZone(Zone.DISCARD, folly);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(friends);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(living));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, living);
		// The event's own effect then asks which card of the discard pile goes beneath the deck.
		scn.FreepsChooseCardBPFromSelection(folly);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
