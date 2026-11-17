package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_158_Tests
{

	// The Fellowship path: Bag End 1_319 at site 1, whose text needs a Hobbit to exert and is never used.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rack", "11_158");
					put("rack2", "11_158");

					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability that needs a mount
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] Man companion, vitality 2; a regroup ability that needs a [rohan] ally

					// Riddermark Javelin: [rohan] ranged weapon, cost 1, on a [rohan] Man.  "Archery: Discard this
					// possession to make the fellowship archery total +1 (or +2 if the fellowship is at a plains site)."
					put("javelin", "11_151");
					put("spear", "4_268");      // Éomer's Spear: [rohan] hand weapon on a [rohan] Man; its text is about Éomer mounted
					put("chain", "4_278");      // Heavy Chain: [rohan] armor, "Bearer must be a Man"; its text is about being overwhelmed
					put("shield", "5_91");      // Rohirrim Shield: [rohan] shield on a [rohan] Man; minion archery total -1, a regroup ability never used here
					// Firefoot, Mearas of The Mark: [rohan] mount on a [rohan] Man; its play trigger and maneuver trigger are never met
					put("firefoot", "13_126");
					put("herugrim", "54_280");  // Herugrim: [rohan] ARTIFACT, hand weapon, vitality +1, "Bearer must be Théoden"
					// Protecting the Hall: [rohan] CONDITION on a [rohan] Man; an assignment ability used only on purpose
					put("hall", "11_149");
					put("armor", "1_92");       // Armor: [gondor] possession, "Bearer must be a Man"; a cap on skirmish wounds

					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc.  "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					// Massing in the East: [sauron] Regroup event, cost 1.  "Exert a [sauron] minion to discard a card from
					// the top of the Free Peoples player's draw deck for each of these races you can spot in the
					// fellowship: Dwarf, Elf, Man, and Wizard."
					put("massing", "3_92");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rack", "11_158");
					put("guard", "5_83");       // Household Guard: [rohan] Man, vitality 3; Valiant is its whole text
					// Heavy Chain: [rohan] armor possession, "Bearer must be a Man"; its text is about being
					// overwhelmed
					put("chain", "4_278");
					// Rohirrim Shield: [rohan] shield possession on a [rohan] Man; its regroup ability is not used
					put("shield", "5_91");
					// Beyond the Height of Men: [isengard] Maneuver event, cost 2.  "Exert an Uruk-hai to discard
					// an armor possession, helm possession, or shield possession (or all such Free Peoples
					// possessions if you can spot 6 companions)."
					put("height", "2_39");
					// Uruk Scout: [isengard] Uruk-hai, vitality 2.  Its Response answers an event that spots or
					// exerts a ranger, and nobody here is one.
					put("scout", "2_47");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rack", "11_158");
					put("guard", "5_83");       // Household Guard: [rohan] Man, vitality 3; Valiant is its whole text
					// Rohirrim Shield: [rohan] possession on a [rohan] Man.  "Regroup: Discard this possession to
					// heal bearer."
					put("shield", "5_91");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordRackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Sword Rack
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If a [rohan] possession is discarded from play and no more than one possession is stacked here, stack it here.<br><b>Fellowship:</b> Add (1) to play a possession stacked here as if from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rack");

		assertEquals("Sword Rack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost whose whole effect would do nothing is not offered.  Once
	// the discarded possession is stacked here it is no longer in the discard pile, and a second use of the
	// Response for the same discard would stack nothing.  The game used to offer it again each time it was taken,
	// for as long as the player went on taking it (F-0357).
	@Test
	public void OnceTheDiscardedPossessionIsStackedHereTheResponseIsNotOfferedForItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var rack = scn.GetFreepsCard("rack");
		var guard = scn.GetFreepsCard("guard");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, shield);
		assertAttachedTo(shield, guard);
		scn.MoveCardsToSupportArea(rack);

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		assertEquals(2, scn.GetWoundsOn(guard));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetStackedCards(rack).size());

		// The shield is discarded from play as the cost of its own ability
		scn.FreepsUseCardAction(shield);
		assertInZone(Zone.DISCARD, shield);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack));

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, shield);
		assertEquals(1, scn.GetStackedCards(rack).size());
		assertTrue(scn.GetStackedCards(rack).contains(shield));

		// Nothing is left in the discard pile for the Response to stack: the shield's own ability goes on, one
		// wound of the two is healed, and it is the Shadow player's turn to act
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rack));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ResponseStacksHereARohanPossessionThatHasJustBeenDiscardedFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowDiscardScenario();

		var rack = scn.GetFreepsCard("rack");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain, shield);
		assertAttachedTo(chain, guard);
		assertAttachedTo(shield, guard);
		scn.MoveCardsToSupportArea(rack);

		var height = scn.GetShadowCard("height");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(height);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertAttachedTo(chain, guard);
		assertAttachedTo(shield, guard);
		assertEquals(0, scn.GetStackedCards(rack).size());
		assertTrue(scn.ShadowPlayAvailable(height));

		// The Scout is the one Uruk-hai and exerts with nothing asked; the Chain and the Shield are the two
		// possessions the event could discard.
		scn.ShadowPlayCard(height);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowHasCardChoiceAvailable(chain, shield));
		scn.ShadowChooseCard(chain);

		// The Chain is in the discard pile already when its owner is offered the Response.
		assertInZone(Zone.DISCARD, chain);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, chain);
		assertEquals(1, scn.GetStackedCards(rack).size());
		assertTrue(scn.GetStackedCards(rack).contains(chain));
		assertAttachedTo(shield, guard);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EitherCopyMayStackARohanPossessionDiscardedWithItsBearerAndNeitherAnswersTheOtherCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rack = scn.GetFreepsCard("rack");
		var rack2 = scn.GetFreepsCard("rack2");
		var theoden = scn.GetFreepsCard("theoden");
		var firefoot = scn.GetFreepsCard("firefoot");
		var herugrim = scn.GetFreepsCard("herugrim");
		var hall = scn.GetFreepsCard("hall");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, firefoot, herugrim, hall, armor);
		scn.MoveCardsToSupportArea(rack, rack2);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		// Vitality 2 + 1 for Herugrim: one wound more kills him.
		scn.AddWoundsToChar(theoden, 2);
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertAttachedTo(firefoot, theoden);
		assertAttachedTo(herugrim, theoden);
		assertAttachedTo(hall, theoden);
		assertAttachedTo(armor, theoden);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetStackedCards(rack).size());
		assertEquals(0, scn.GetStackedCards(rack2).size());

		// Théoden is the one companion the Slayer can wound, and dies with all he bears.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, theoden);
		assertInZone(Zone.DISCARD, firefoot, herugrim, hall, armor);

		// The [rohan] possession: each copy offers to stack it, and the one taken holds it.  The [rohan] artifact, the
		// [rohan] condition and the [gondor] possession are not answered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack2));
		scn.FreepsChooseOptionalTrigger(rack2);
		assertInZone(Zone.STACKED, firefoot);
		assertTrue(scn.GetStackedCards(rack2).contains(firefoot));
		assertEquals(1, scn.GetStackedCards(rack2).size());
		assertEquals(0, scn.GetStackedCards(rack).size());
		assertInZone(Zone.DISCARD, herugrim, hall, armor);

		// With Firefoot stacked, the other copy has nothing left to stack.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rack));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoPossessionsStackedHereAndOfferedWithOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rack = scn.GetFreepsCard("rack");
		var rack2 = scn.GetFreepsCard("rack2");
		var rider = scn.GetFreepsCard("rider");
		var javelin = scn.GetFreepsCard("javelin");
		var spear = scn.GetFreepsCard("spear");
		var chain = scn.GetFreepsCard("chain");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, javelin);
		scn.MoveCardsToSupportArea(rack, rack2);
		scn.StackCardsOn(rack, spear, chain);
		scn.StackCardsOn(rack2, shield);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertAttachedTo(javelin, rider);
		assertInZone(Zone.STACKED, spear, chain, shield);
		assertEquals(2, scn.GetStackedCards(rack).size());
		assertEquals(1, scn.GetStackedCards(rack2).size());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The Javelin is discarded from play as the cost of its own ability.
		scn.FreepsUseCardAction(javelin);
		assertInZone(Zone.DISCARD, javelin);
		// The copy with two possessions stacked on it does not answer; the copy with one does.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rack));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack2));
		scn.FreepsChooseOptionalTrigger(rack2);

		assertTrue(scn.GetStackedCards(rack2).contains(javelin));
		assertEquals(2, scn.GetStackedCards(rack2).size());
		assertEquals(2, scn.GetStackedCards(rack).size());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rack));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedForARohanPossessionDiscardedFromTheTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rack = scn.GetFreepsCard("rack");
		var rider = scn.GetFreepsCard("rider");
		var javelin = scn.GetFreepsCard("javelin");
		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToSupportArea(rack);

		var slayer = scn.GetShadowCard("slayer");
		var massing = scn.GetShadowCard("massing");
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(javelin);
		assertInZone(Zone.DECK, javelin);
		assertSame(javelin, scn.GetFreepsTopOfDeck());
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);
		scn.FreepsRefreshDecision();
		assertSame(javelin, scn.GetFreepsTopOfDeck());

		// One race in the fellowship, Man: one card off the top of the deck, the Javelin.
		scn.FreepsPass();
		scn.ShadowPlayCard(massing);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertInZone(Zone.DISCARD, javelin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rack));
		assertEquals(0, scn.GetStackedCards(rack).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysAPossessionStackedOnThisCopyOnARohanManOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var rack = scn.GetFreepsCard("rack");
		var rack2 = scn.GetFreepsCard("rack2");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var javelin = scn.GetFreepsCard("javelin");
		var spear = scn.GetFreepsCard("spear");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToSupportArea(rack, rack2);
		scn.StackCardsOn(rack, javelin, spear);
		scn.StackCardsOn(rack2, shield);

		scn.StartGame();

		assertInZone(Zone.STACKED, javelin, spear, shield);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(rack));

		scn.FreepsUseCardAction(rack);
		// The possessions stacked on this copy, and not the one on the other.
		assertTrue(scn.FreepsHasCardChoiceAvailable(javelin, spear));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shield));
		scn.FreepsChooseCard(javelin);

		// Played as if from hand: on a [rohan] Man of the player's choice, companion or ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rider);

		// 1 added by this card + 1 for the Javelin.
		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(javelin, rider);
		assertTrue(scn.GetStackedCards(rack).contains(spear));
		assertEquals(1, scn.GetStackedCards(rack).size());
		assertTrue(scn.GetStackedCards(rack2).contains(shield));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWhenTheStackedPossessionHasNoBearerToGoOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rack = scn.GetFreepsCard("rack");
		var boromir = scn.GetFreepsCard("boromir");
		var javelin = scn.GetFreepsCard("javelin");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(rack);
		scn.StackCardsOn(rack, javelin);

		scn.StartGame();

		// A Man of [gondor] and Frodo: no [rohan] Man to bear the Javelin.
		assertInZone(Zone.STACKED, javelin);
		assertTrue(scn.GetStackedCards(rack).contains(javelin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rack));
	}
}
