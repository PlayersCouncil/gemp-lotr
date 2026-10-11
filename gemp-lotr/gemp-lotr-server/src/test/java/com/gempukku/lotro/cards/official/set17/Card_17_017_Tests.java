package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_17_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "17_17");

					put("radagast", "9_26");     // Radagast, The Brown: the other [gandalf] Wizard, who could bear a Wizard Staff or the Herb Bag
					put("grimbeorn", "14_6");    // Grimbeorn: a [gandalf] Man, cost 3, the bearer a Beorning Axe wants; heals himself as a turn starts
					put("elf", "1_53");          // Lórien Elf: the Elf that Elven Cloak wants spotted
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8

					// [gandalf] possessions Gandalf may bear
					put("glamdring", "1_75");    // Glamdring: hand weapon, cost 2; bearer must be Gandalf
					put("wizardstaff", "2_29");  // Wizard Staff: staff, cost 2, strength +1; bearer must be a Wizard
					put("pipe", "1_74");         // Gandalf's Pipe: pipe, cost 0; bearer must be Gandalf
					put("bag", "18_26");         // Radagast's Herb Bag: cost 1, vitality +1; bearer must be a [gandalf] Wizard
					// Cards that are not "a [gandalf] possession on him", a copy for each pile
					put("staff", "4_91");        // Gandalf's Staff, Walking Stick: a [gandalf] ARTIFACT; bearer must be Gandalf
					put("staff2", "4_91");
					put("cloak", "1_42");        // Elven Cloak: an [elven] possession for any companion (to play, spot an Elf)
					put("cloak2", "1_42");
					put("axe", "18_18");         // Beorning Axe: a [gandalf] possession for a [gandalf] Man
					put("axe2", "18_18");
					put("cart", "1_73");         // Gandalf's Cart: a [gandalf] possession played to the support area
					put("cart2", "1_73");

					// Cards that hold culture tokens
					put("evenstar", "15_16");    // Gift of the Evenstar: [elven] artifact, support area; its triggers want an Elf's win or a maneuver phase
					put("wonder", "13_30");      // Fear and Great Wonder: [gandalf] condition; a maneuver ability used only on purpose
					put("hat", "0_60");          // Tom Bombadil's Hat: [shire] possession, support area; takes a token when a Shadow card adds a burden
					put("heirs", "13_69");       // Heirs of Gondor: [gondor] condition; here it has no token
					put("ships", "8_65");        // Ships of Great Draught: the Shadow player's [raider] possession, support area

					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1
					put("pursuer", "4_190");     // Uruk Pursuer: fierce, strength 5, vitality 3; his texts are about unbound Hobbits
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Gandalf, Returned
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: When you play Gandalf (except in your starting fellowship), you may play a [gandalf] possession on him from your draw deck or discard pile.<br>Each time Gandalf wins a skirmish, you may reinforce a Free Peoples token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Returned", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedFromHandForFourHeMayPlayAGandalfPossessionOnHimselfFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var glamdring = scn.GetFreepsCard("glamdring");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var pipe = scn.GetFreepsCard("pipe");
		var bag = scn.GetFreepsCard("bag");
		var staff = scn.GetFreepsCard("staff");
		var cloak = scn.GetFreepsCard("cloak");
		var axe = scn.GetFreepsCard("axe");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCompanionsToTable(radagast);
		scn.MoveCardsToHand(gandalf);
		// Two of the possessions he may bear lie in the discard pile, with a copy of each card he may not take;
		// the other two, and the other copies, are in the draw deck.
		scn.MoveCardsToDiscard(pipe, bag);
		scn.MoveCardsToFreepsDiscard("staff2", "cloak2", "axe2", "cart2");

		scn.StartGame();
		// A bearer for the Beorning Axe and an Elf for the Elven Cloak: each could be played, but not on Gandalf.
		scn.MoveCompanionsToTable("grimbeorn", "elf");

		var freepsShuffles = scn.GetFreepsDeckShuffleCount();
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gandalf));
		scn.FreepsPlayCard(gandalf);

		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();

		// Each pile holds a card he could play: the pile is the player's choice.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChoose("draw deck");
		scn.FreepsDismissRevealedCards();

		// The artifact, the [elven] possession, the Axe that goes on a [gandalf] Man and the Cart that goes to the
		// support area are not offered; nor are the two possessions in the other pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, wizardstaff));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(staff, cloak, axe, cart, pipe, bag));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(wizardstaff);

		// On him, though Radagast is a Wizard too; its own cost is paid.  4 for Gandalf + 2 for the Staff
		assertAttachedTo(wizardstaff, gandalf);
		assertEquals(6, scn.GetTwilight());
		assertEquals(7 + 1, scn.GetStrength(gandalf));
		assertInZone(Zone.DECK, glamdring);
		// The draw deck was looked through, and is shuffled; the other player's is not.
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HeMayPlayAGandalfPossessionOnHimselfFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var glamdring = scn.GetFreepsCard("glamdring");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var pipe = scn.GetFreepsCard("pipe");
		var bag = scn.GetFreepsCard("bag");
		var staff2 = scn.GetFreepsCard("staff2");
		var cloak2 = scn.GetFreepsCard("cloak2");
		var axe2 = scn.GetFreepsCard("axe2");
		var cart2 = scn.GetFreepsCard("cart2");
		scn.MoveCompanionsToTable(radagast);
		scn.MoveCardsToHand(gandalf);
		scn.MoveCardsToDiscard(pipe, bag, staff2, cloak2, axe2, cart2);

		scn.StartGame();
		scn.MoveCompanionsToTable("grimbeorn", "elf");

		var freepsShuffles = scn.GetFreepsDeckShuffleCount();
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetTwilight());
		assertEquals(4, scn.GetVitality(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChoose("discard");

		// The two possessions he may bear, and none of the other four cards of that pile; nothing from the draw deck.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, bag));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(staff2, cloak2, axe2, cart2, glamdring, wizardstaff));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(bag);

		// On him, though Radagast could bear it too.  4 for Gandalf + 1 for the Herb Bag
		assertAttachedTo(bag, gandalf);
		assertEquals(5, scn.GetTwilight());
		assertEquals(4 + 1, scn.GetVitality(gandalf));
		assertInDiscard(pipe);
		// No draw deck was looked through.
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingAndLooksThroughNoPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var pipe = scn.GetFreepsCard("pipe");
		var bag = scn.GetFreepsCard("bag");
		scn.MoveCardsToHand(gandalf);
		scn.MoveCardsToDiscard(pipe, bag);

		scn.StartGame();

		var freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetAttachedCards(gandalf).size());
		assertInDiscard(pipe, bag);
		assertInZone(Zone.DECK, wizardstaff);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingToPlayInTheDiscardPileTheDrawDeckIsLookedThroughUnaskedAndNothingNeedBeTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var pipe = scn.GetFreepsCard("pipe");
		var bag = scn.GetFreepsCard("bag");
		// The Pipe and the Herb Bag are in hand, so in neither pile; the discard pile holds only cards he may not take.
		scn.MoveCardsToHand(gandalf, pipe, bag);
		scn.MoveCardsToFreepsDiscard("staff2", "cloak2", "axe2", "cart2");

		scn.StartGame();
		scn.MoveCompanionsToTable("grimbeorn", "elf");

		var freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();

		// No pile is asked for: the deck is shown at once.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, wizardstaff));
		// A player who looks through his draw deck need not take what he finds there.
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetAttachedCards(gandalf).size());
		assertInZone(Zone.DECK, glamdring, wizardstaff);
		// Looked through, so shuffled, though nothing was played from it.
		assertTrue(scn.GetFreepsDeckShuffleCount() > freepsShuffles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingToPlayInTheDrawDeckThePossessionComesFromTheDiscardPileUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var pipe = scn.GetFreepsCard("pipe");
		var bag = scn.GetFreepsCard("bag");
		// Glamdring and the Wizard Staff are in hand, so in neither pile; the draw deck holds only cards he may not take.
		scn.MoveCardsToHand(gandalf, glamdring, wizardstaff);
		scn.MoveCardsToDiscard(pipe, bag);
		scn.MoveCardsToFreepsDiscard("staff2", "cloak2", "axe2", "cart2");

		scn.StartGame();
		scn.MoveCompanionsToTable("grimbeorn", "elf");

		var freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();

		// No pile is asked for, and no deck is shown: the choice is of the two cards in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, bag));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(pipe);

		assertAttachedTo(pipe, gandalf);
		assertInDiscard(bag);
		// Gandalf's Pipe costs nothing.
		assertEquals(4, scn.GetTwilight());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenNeitherPileHoldsAGandalfPossessionHeMayBear() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		var pipe = scn.GetFreepsCard("pipe");
		var bag = scn.GetFreepsCard("bag");
		// Every possession he may bear is in hand.  Each pile holds the artifact, the [elven] possession, the Axe
		// and the Cart.
		scn.MoveCardsToHand(gandalf, glamdring, wizardstaff, pipe, bag);
		scn.MoveCardsToFreepsDiscard("staff2", "cloak2", "axe2", "cart2");

		scn.StartGame();
		scn.MoveCompanionsToTable("grimbeorn", "elf");

		scn.FreepsPlayCard(gandalf);

		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenHeIsPlayedInTheStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");

		// Every card is in the draw deck, the possessions he may bear among them.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, gandalf, glamdring, wizardstaff);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
		scn.FreepsChooseCard(gandalf);

		// His 4 is the whole of the 4 allowed, so the choice is over; nothing is offered him, and the other
		// player is choosing now.
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetAttachedCards(gandalf).size());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void AnotherCompanionPlayedWhileGandalfIsInPlayBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var grimbeorn = scn.GetFreepsCard("grimbeorn");
		var wizardstaff = scn.GetFreepsCard("wizardstaff");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(grimbeorn);

		scn.StartGame();

		assertInZone(Zone.DECK, wizardstaff);
		assertTrue(scn.FreepsPlayAvailable(grimbeorn));
		scn.FreepsPlayCard(grimbeorn);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, grimbeorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeGandalfWinsASkirmishHeMayReinforceAFreePeoplesToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var evenstar = scn.GetFreepsCard("evenstar");
		var wonder = scn.GetFreepsCard("wonder");
		var hat = scn.GetFreepsCard("hat");
		var heirs = scn.GetFreepsCard("heirs");
		var ships = scn.GetShadowCard("ships");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(evenstar, wonder, hat, heirs, ships);

		scn.StartGame();
		// Tokens of three Free Peoples cultures on an artifact, a condition and a possession; a [gondor] condition
		// with none; and a [raider] token on the Shadow player's possession.
		scn.AddTokensToCard(evenstar, 1);
		scn.AddTokensToCard(wonder, 1);
		scn.AddTokensToCard(hat, 1);
		scn.AddTokensToCard(ships, 1);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassCurrentPhaseActions();

		// 7 against 5
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetCultureTokensOn(evenstar));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(evenstar, wonder, hat));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heirs, ships));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(hat);

		assertEquals(2, scn.GetCultureTokensOn(hat));
		assertEquals(1, scn.GetCultureTokensOn(evenstar));
		assertEquals(1, scn.GetCultureTokensOn(wonder));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(ships));

		// The Pursuer is fierce: Gandalf wins a second skirmish against him, and is offered it again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, pursuer);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: every count is as it was.
		assertEquals(2, scn.GetCultureTokensOn(hat));
		assertEquals(1, scn.GetCultureTokensOn(evenstar));
		assertEquals(1, scn.GetCultureTokensOn(wonder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenGandalfLosesASkirmishOrAnotherCompanionWinsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wonder = scn.GetFreepsCard("wonder");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(wonder);

		scn.StartGame();
		scn.AddTokensToCard(wonder, 1);
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{gandalf, troop});

		// Aragorn wins, 8 against 5, with Gandalf on the table.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Gandalf loses, 7 against 13.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(wonder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GandalfDoesNotReinforceBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(gandalf);
		// A Free Peoples card that could hold a token and has none.
		scn.MoveCardsToSupportArea(heirs);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassCurrentPhaseActions();

		// A burden is no Free Peoples token: with no culture token in play there is nothing to reinforce, and the
		// offer is not made (Q-0034; withheld by the engine since Q-0224).
		assertInDiscard(runner);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
