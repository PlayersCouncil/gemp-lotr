package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_154_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_154");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uruk Slaughterer and a second [uruk-hai] minion against two Dwarf Guards, each overwhelmed and killed.
	protected VirtualTableScenario GetKillScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaughterer", "12_154");
					// Suppressing Uruk: [uruk-hai] Uruk-hai, strength 11; its text lowers resistance and nothing else
					put("suppressing", "12_147");
					put("guard1", "1_7");       // Dwarf Guard: strength 4, vitality 2
					put("guard2", "1_7");
					put("scout", "15_116");     // Scouting Orc: [orc] Orc; "Skirmish: Exert this minion to wound a companion it is skirmishing."
					for (int i = 1; i <= 5; i++) {
						put("filler" + i, "1_177");     // Goblin Patrol Troop: no text at all
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSlaughtererStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Slaughterer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time a companion is killed in a skirmish involving an [uruk-hai] minion, you may discard any number of cards from hand to draw the same number of cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Uruk Slaughterer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// "You may discard any number of cards from hand to draw the same number of cards": the discard stands
	// before the "to", and on an optional trigger with nothing else to pay it is of at least one card (the shape
	// of F-0300, Robin Smallburrow 18_111).  The definition asked for any number from none: the offer was made
	// with an empty hand, and once taken could be answered with no card, a second way of saying no.
	// F-0449.
	@Test
	public void TheOfferIsNotMadeWithAnEmptyHandAndOnceTakenDiscardsAtLeastOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKillScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var suppressing = scn.GetShadowCard("suppressing");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		scn.MoveMinionsToTable(slaughterer, suppressing);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, slaughterer}, new PhysicalCardImpl[]{guard2, suppressing});

		// The first Dwarf is killed with no card in the Shadow player's hand, and cards in his draw deck.
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The second is killed by the other [uruk-hai] minion, with two cards in that hand.
		scn.SetShadowHand(filler1, filler2);
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		int deck = scn.GetShadowDeckCount();
		scn.ShadowChooseCards(filler1, filler2);

		// Two discarded and two drawn.
		assertInZone(Zone.DISCARD, filler1, filler2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// "Killed in a skirmish involving an [uruk-hai] minion" is any kill in that skirmish, whatever wound dealt it,
	// and not "killed by an [uruk-hai] minion" (CR 4.2's definition of that is for "<X> kills", Q-0328). The card
	// was built with `killer: culture(uruk-hai),minion`: this kill, by another minion's own wound, was not
	// answered. F-0547.
	@Test
	public void ACompanionKilledInTheSkirmishByAnotherMinionsWoundOffersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKillScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(guard1);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var scout = scn.GetShadowCard("scout");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		scn.AddWoundsToChar(guard1, 1);
		scn.MoveMinionsToTable(slaughterer, scout);
		scn.SetShadowHand(filler1, filler2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, slaughterer);
		// The Shadow player puts the [orc] Orc in the same skirmish.
		scn.ShadowAssignToMinions(guard1, scout);
		scn.FreepsResolveSkirmish(guard1);

		scn.FreepsPass();
		scn.ShadowUseCardAction(scout);
		// The Orc's wound kills the Dwarf before the skirmish is resolved.
		assertInZone(Zone.DEAD, guard1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowAcceptOptionalTrigger();

		int deck = scn.GetShadowDeckCount();
		scn.ShadowChooseCards(filler1, filler2);
		assertInZone(Zone.DISCARD, filler1, filler2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		// The Dwarf was the skirmish's only Free Peoples character: it is over, and so is the phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void UrukSlaughtererTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(4, scn.GetTwilight());
	}
}
