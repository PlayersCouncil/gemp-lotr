package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_026_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crows", "101_26");
					put("crows2", "101_26");

					// Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here
					put("aragorn", "1_89");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability never used here
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; a maneuver ability never used here
					// Anduril, King's Blade: Free Peoples artifact borne by Aragorn; makes him defender +1
					put("anduril", "7_80");
					put("coat", "1_101");       // Coat of Mail: Free Peoples possession borne by a Man; about being overwhelmed only
					put("shield", "1_107");     // Great Shield: Free Peoples possession borne by a Man; minion archery total -1
					// The Saga of Elendil: Free Peoples condition borne by a [gondor] companion; a skirmish ability
					put("saga", "1_114");
					put("armor1", "1_8");       // Dwarven Armor: Free Peoples possession borne by a Dwarf; about being overwhelmed only
					put("armor2", "1_8");
					// Spider Poison: a Shadow possession that a companion bears; it only answers its bearer
					// being assigned to a skirmish
					put("poison", "8_28");

					put("sword", "1_160");      // Uruk-hai Sword: [isengard] possession
					put("broadsword", "4_142"); // Broad-bladed Sword: [isengard] possession
					put("palantir", "3_67");    // The Palantir of Orthanc: [isengard] artifact
					put("staff", "4_174");      // Saruman's Staff, Wizard's Device: [isengard] artifact
					put("spear", "4_196");      // Uruk Spear: [isengard] possession
					// Saruman's Ambition: [isengard] condition, cost 0, in the support area.  Not an item, and
					// not Saruman.  It makes [isengard] events cheaper, and there are none here.
					put("ambition", "1_133");
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, cost 2, site 5; damage +1 is its whole text
					// Saruman, Agent of the Dark Lord: [uruk-hai] minion, cost 4, site 4, lurker; a skirmish
					// ability and a trigger on a companion's death, neither met here
					put("saruman", "12_144");
					// Saruman, Servant of Sauron: unique [isengard] Shadow follower, cost 1, played to the support
					// area.  "Aid - (1)": at the start of the maneuver phase it may be transferred to a minion for
					// (1).  "Each time bearer wins a skirmish, you may exert a companion."  Neither is met in a
					// Shadow phase.
					put("servant", "17_38");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrowsofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Crows of Isengard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Crow
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play this minion, spot a companion to add (1) for each Free Peoples card borne by that companion.
		 * 	Shadow: Spot 2 Free Peoples items (or spot Saruman) to take an [isengard] item into hand from your draw deck or discard pile (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crows");

		assertEquals("Crows of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CROW, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AddsOneTwilightForEachFreePeoplesCardTheSpottedCompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var crows = scn.GetShadowCard("crows");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		// An artifact, two possessions and a condition of the Free Peoples, and one Shadow possession
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("anduril"), scn.GetFreepsCard("coat"),
				scn.GetFreepsCard("shield"), scn.GetFreepsCard("saga"), scn.GetShadowCard("poison"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor1"));
		scn.AttachCardsTo(thrarin, scn.GetFreepsCard("armor2"));
		scn.MoveCardsToHand(crows);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 4 set + 3 companions + 2 for the shadow number of site 2
		assertEquals(9, scn.GetTwilight());
		assertEquals(5, scn.GetAttachedCards(aragorn).size());
		assertInZone(Zone.HAND, crows);

		scn.ShadowPlayCard(crows);

		assertInZone(Zone.SHADOW_CHARACTERS, crows);
		// Any companion may be spotted, the Ring-bearer too; an ally who bears a possession is not a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thrarin));
		// 9 - 3 to play - 2 roaming (a site 4 minion played at site 2), and nothing added yet
		assertEquals(4, scn.GetTwilight());
		scn.ShadowChooseCard(aragorn);

		// 4 + 4: Anduril, the Coat of Mail, the Great Shield and the Saga of Elendil, and not the Spider Poison
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CountsOnlyTheCardsOfTheCompanionSpottedAndTheOneRingIsNotOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var crows = scn.GetShadowCard("crows");
		var crows2 = scn.GetShadowCard("crows2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("anduril"), scn.GetFreepsCard("coat"),
				scn.GetFreepsCard("shield"), scn.GetFreepsCard("saga"), scn.GetShadowCard("poison"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor1"));
		scn.AttachCardsTo(thrarin, scn.GetFreepsCard("armor2"));
		scn.MoveCardsToHand(crows, crows2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 3 companions + 2 for the shadow number of site 2
		assertEquals(15, scn.GetTwilight());

		scn.ShadowPlayCard(crows);
		// 15 - 3 to play - 2 roaming
		assertEquals(10, scn.GetTwilight());
		scn.ShadowChooseCard(gimli);

		// Gimli's one Dwarven Armor: not Aragorn's four cards, and not the Dwarven Armor on Thrarin
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(crows2);
		// 11 - 3 to play - 2 roaming
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, frodo));
		assertEquals(1, scn.GetAttachedCards(frodo).size());
		assertAttachedTo(scn.GetRing(), frodo);
		scn.ShadowChooseCard(frodo);

		// The One Ring is neither a Free Peoples card nor a Shadow card, so Frodo bears none.  The copy
		// already in play asks for nothing and adds nothing.
		assertEquals(6, scn.GetTwilight());
		assertInPlay(crows, crows2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedWithCrowsInPlayAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crows = scn.GetShadowCard("crows");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("coat"), scn.GetFreepsCard("shield"));
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		scn.MoveMinionsToTable(crows);
		scn.SetTwilight(6);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 6 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInPlay(crows);

		scn.ShadowPlayCard(savage);

		assertInPlay(savage);
		// 10 - 2 to play - 2 roaming (a site 5 minion played at site 2), with nobody spotted and nothing added
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTwoFreePeoplesItemsTakesAnIsengardItemFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crows = scn.GetShadowCard("crows");
		var crows2 = scn.GetShadowCard("crows2");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var palantir = scn.GetShadowCard("palantir");
		var staff = scn.GetShadowCard("staff");
		var broadsword = scn.GetShadowCard("broadsword");
		var ambition = scn.GetShadowCard("ambition");
		var scimitar = scn.GetShadowCard("scimitar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(aragorn);
		// One Free Peoples item, a Free Peoples condition and a Shadow possession; Frodo has The One Ring.
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("coat"), scn.GetFreepsCard("saga"), scn.GetShadowCard("poison"));
		scn.MoveCardsToHand(savage);
		// The other [isengard] items stay in the draw deck; this one gives the discard pile an item too.
		scn.MoveCardsToDiscard(broadsword);

		scn.StartGame();
		scn.MoveMinionsToTable(crows);
		scn.SetTwilight(5);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(3, scn.GetAttachedCards(aragorn).size());
		assertFalse(scn.ShadowActionAvailable(crows));

		// A second item, an artifact this time.  Playing the Uruk Savage is only there to have the game ask again.
		scn.AttachCardsTo(aragorn, anduril);
		scn.ShadowPlayCard(savage);
		assertTrue(scn.ShadowActionAvailable(crows));

		assertEquals(0, scn.GetShadowHandCount());
		int deckBefore = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		int opponentShuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DECK, sword, palantir, staff);

		scn.ShadowUseCardAction(crows);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();

		// A possession and two artifacts
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, palantir, staff));
		// An [isengard] condition, an [isengard] minion, and a possession that is not [isengard]
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ambition, crows2, scimitar));
		scn.ShadowChooseCardBPFromSelection(palantir);

		assertInZone(Zone.HAND, palantir);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, sword, staff);
		assertInZone(Zone.DISCARD, broadsword);
		// A draw deck that has been looked through is reshuffled, once, and nobody else's is touched.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingSarumanIsEnoughWithOneFreePeoplesItem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crows = scn.GetShadowCard("crows");
		// "Saruman" is a title: this one is not [isengard], unlike the Crows.
		var saruman = scn.GetShadowCard("saruman");
		var ambition = scn.GetShadowCard("ambition");
		var scimitar = scn.GetShadowCard("scimitar");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("coat"));
		scn.MoveCardsToHand(saruman);
		// Saruman's Ambition is not Saruman.
		scn.MoveCardsToSupportArea(ambition);
		// The discard pile holds an item, and none that is [isengard].
		scn.MoveCardsToDiscard(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(crows);
		scn.SetTwilight(2);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());
		assertInPlay(ambition);
		assertFalse(scn.ShadowActionAvailable(crows));

		scn.ShadowPlayCard(saruman);
		assertInPlay(saruman);
		// 6 - 4 to play Saruman - 2 roaming at site 2
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(crows));

		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(crows);
		// With no [isengard] item in the discard pile, only the draw deck can be carried out.
		assertFalse(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(sword);

		assertInZone(Zone.HAND, sword);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, scimitar);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingAFollowerNamedSarumanIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crows = scn.GetShadowCard("crows");
		// The text says "spot Saruman" and no more, so any card of that title in play will do: this one is a
		// follower in the support area, where the other test's Saruman is an [uruk-hai] minion.
		var servant = scn.GetShadowCard("servant");
		var broadsword = scn.GetShadowCard("broadsword");
		var staff = scn.GetShadowCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("coat"));
		scn.MoveCardsToHand(servant);
		scn.MoveCardsToDiscard(broadsword, staff);

		scn.StartGame();
		scn.MoveMinionsToTable(crows);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 companions + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		// One Free Peoples item, and Saruman only in hand
		assertInZone(Zone.HAND, servant);
		assertFalse(scn.ShadowActionAvailable(crows));

		scn.ShadowPlayCard(servant);
		assertInZone(Zone.SUPPORT, servant);
		// 4 - 1 to play the follower, which does not roam
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(crows));

		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(crows);
		scn.ShadowChoose("discard pile");
		assertTrue(scn.ShadowHasCardChoiceAvailable(broadsword, staff));
		scn.ShadowChooseCard(staff);

		assertInZone(Zone.HAND, staff);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, broadsword);
		// Spotting is not a cost: the follower is where it was.
		assertInZone(Zone.SUPPORT, servant);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyTakesFromTheDiscardPileOnceInAPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crows = scn.GetShadowCard("crows");
		var crows2 = scn.GetShadowCard("crows2");
		var broadsword = scn.GetShadowCard("broadsword");
		var staff = scn.GetShadowCard("staff");
		var ambition = scn.GetShadowCard("ambition");
		var scimitar = scn.GetShadowCard("scimitar");
		var savage = scn.GetShadowCard("savage");
		// The Free Peoples player's own copy of an [isengard] possession, in that player's discard pile
		var theirSpear = scn.GetFreepsCard("spear");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);
		// Two possessions, one on a companion and one on an ally
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor1"));
		scn.AttachCardsTo(thrarin, scn.GetFreepsCard("armor2"));
		// A possession and an artifact are the [isengard] items in the discard pile; the Uruk-hai Sword, the
		// Palantir and the Shadow player's own Uruk Spear stay in the draw deck.
		scn.MoveCardsToDiscard(broadsword, staff, ambition, scimitar, savage);
		scn.MoveCardsToDiscard(theirSpear);

		scn.StartGame();
		scn.MoveMinionsToTable(crows, crows2);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// One action for each copy.
		assertTrue(scn.ShadowActionAvailable(crows));
		assertTrue(scn.ShadowActionAvailable(crows2));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowUseCardAction(crows);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");

		assertTrue(scn.ShadowHasCardChoiceAvailable(broadsword, staff));
		// An [isengard] condition, an [isengard] minion, a possession that is not [isengard], and an
		// [isengard] possession in the other player's discard pile
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ambition, savage, scimitar, theirSpear));
		scn.ShadowChooseCard(broadsword);

		assertInZone(Zone.HAND, broadsword);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());
		// The first copy has had its once; the limit of the second copy is its own (CR 4.2 "limit").
		assertFalse(scn.ShadowActionAvailable(crows));
		assertTrue(scn.ShadowActionAvailable(crows2));

		scn.ShadowUseCardAction(crows2);
		scn.ShadowChoose("discard pile");
		// The Staff is the only [isengard] item left in the discard pile, so it is taken without asking.
		assertInZone(Zone.HAND, staff);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, ambition, savage, scimitar, theirSpear);
		assertFalse(scn.ShadowActionAvailable(crows));
		assertFalse(scn.ShadowActionAvailable(crows2));
		// The draw deck was not looked through either time, so it is not shuffled.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBeUsedAgainInASecondShadowPhaseOfTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crows = scn.GetShadowCard("crows");
		var broadsword = scn.GetShadowCard("broadsword");
		var staff = scn.GetShadowCard("staff");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("armor1"));
		scn.AttachCardsTo(thrarin, scn.GetFreepsCard("armor2"));
		scn.MoveCardsToDiscard(broadsword, staff);

		scn.StartGame();
		scn.MoveMinionsToTable(crows);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowActionAvailable(crows));

		scn.ShadowUseCardAction(crows);
		scn.ShadowChoose("discard pile");
		scn.ShadowChooseCard(broadsword);
		assertInZone(Zone.HAND, broadsword);
		assertFalse(scn.ShadowActionAvailable(crows));

		// Nobody is assigned to the Crows, so they are still in play when the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInPlay(crows);
		assertTrue(scn.ShadowActionAvailable(crows));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
