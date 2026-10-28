package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "7_37");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his ability is never used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who exerts to take part in skirmishes
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("savage2", "1_151");
					// Cards in hand to place on a draw deck, of three types and cultures.
					put("elf", "1_53");         // Lorien Elf: an [elven] companion
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] possession
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession
					put("elf2", "1_53");
					put("axe2", "1_9");
					put("elf3", "1_53");
					put("axe3", "1_9");
					put("deftness2", "1_293");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gandalf, Manager of Wizards
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Skirmish:</b> Place 3 cards from hand on top of your draw deck to make a minion skirmishing an unbound companion strength -3. Any Shadow player may place 3 cards from his or her hand on top of his or her draw deck to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Manager of Wizards", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet()); 
	}

	@Test
	public void SkirmishAbilityPlacesThreeCardsOnTheDeckToMakeAMinionSkirmishingGandalfStrengthMinus3ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var deftness = scn.GetFreepsCard("deftness");
		var cloak = scn.GetFreepsCard("cloak");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var shadowElf = scn.GetShadowCard("elf");
		var shadowAxe = scn.GetShadowCard("axe");
		var shadowCloak = scn.GetShadowCard("cloak");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(elf, axe, deftness, cloak);
		scn.MoveCardsToHand(shadowElf, shadowAxe, shadowCloak);
		assertInHand(elf, axe, deftness, cloak, shadowElf, shadowAxe, shadowCloak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.ShadowAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(4, scn.GetFreepsHandCount());
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		assertTrue(scn.FreepsActionAvailable(gandalf));
		scn.FreepsUseCardAction(gandalf);

		// Any three cards from hand: a companion, a possession and an event are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, axe, deftness, cloak));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(elf, axe, deftness);
		scn.FreepsChooseCard(elf);
		scn.FreepsChooseCard(axe);

		// The three are on top of the Free Peoples draw deck, the last placed on top.
		assertInHand(cloak);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		assertEquals(deftness, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(axe, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(elf, scn.GetFromTopOfFreepsDeck(3));

		// The Shadow player declines to prevent it: his hand and deck are as they were.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());

		// Either minion of the skirmish, of two cultures and races; not the one waiting for Aragorn's skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		scn.FreepsChooseCard(troop);

		assertEquals(10, scn.GetStrength(troop));    // 13 - 3
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf (7) is overwhelmed by 10 + 5; the minions, who won, are left on the table.
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, gandalf);
		assertInPlay(troop, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// The next skirmish is a phase of its own: the -3 is gone.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void UsedInAnotherUnboundCompanionsSkirmishTheShadowPlayerMayPlaceThreeCardsOnHisDeckToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var deftness = scn.GetFreepsCard("deftness");
		var troop = scn.GetShadowCard("troop");
		var shadowElf = scn.GetShadowCard("elf");
		var shadowAxe = scn.GetShadowCard("axe");
		var shadowDeftness = scn.GetShadowCard("deftness");
		var shadowCloak = scn.GetShadowCard("cloak");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(elf, axe, deftness);
		scn.MoveCardsToHand(shadowElf, shadowAxe, shadowDeftness, shadowCloak);
		assertInHand(elf, axe, deftness, shadowElf, shadowAxe, shadowDeftness, shadowCloak);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(troop));
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		// With three cards in hand, all three go unasked; only their order is asked.
		scn.FreepsUseCardAction(gandalf);
		scn.FreepsChooseCard(elf);
		scn.FreepsChooseCard(axe);

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowElf, shadowAxe, shadowDeftness, shadowCloak));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(shadowElf, shadowAxe, shadowDeftness);
		scn.ShadowChooseCard(shadowElf);
		scn.ShadowChooseCard(shadowAxe);

		// His three cards are on top of his own draw deck.
		assertInHand(shadowCloak);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck + 3, scn.GetShadowDeckCount());
		assertEquals(shadowDeftness, scn.GetFromTopOfShadowDeck(1));
		assertEquals(shadowAxe, scn.GetFromTopOfShadowDeck(2));
		assertEquals(shadowElf, scn.GetFromTopOfShadowDeck(3));

		// The Free Peoples cost stays paid, and the minion is not weakened.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithTwoCardsInTheShadowHandThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var deftness = scn.GetFreepsCard("deftness");
		var troop = scn.GetShadowCard("troop");
		var shadowElf = scn.GetShadowCard("elf");
		var shadowAxe = scn.GetShadowCard("axe");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(elf, axe, deftness);
		scn.MoveCardsToHand(shadowElf, shadowAxe);
		assertInHand(elf, axe, deftness, shadowElf, shadowAxe);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(13, scn.GetStrength(troop));
		// With three cards in hand, all three go unasked; only their order is asked.
		scn.FreepsUseCardAction(gandalf);
		scn.FreepsChooseCard(elf);
		scn.FreepsChooseCard(axe);

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(10, scn.GetStrength(troop));    // 13 - 3
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsASkirmishAbilityAndNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var deftness = scn.GetFreepsCard("deftness");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(elf, axe, deftness);
		assertInHand(elf, axe, deftness);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void WithTwoCardsInHandTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(elf, axe);
		assertInHand(elf, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void InTheSkirmishOfAnAllyARingBoundCompanionOrTheRingBearerTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elf = scn.GetFreepsCard("elf");
		var axe = scn.GetFreepsCard("axe");
		var deftness = scn.GetFreepsCard("deftness");
		var cloak = scn.GetFreepsCard("cloak");
		var elf2 = scn.GetFreepsCard("elf2");
		var axe2 = scn.GetFreepsCard("axe2");
		var elf3 = scn.GetFreepsCard("elf3");
		var axe3 = scn.GetFreepsCard("axe3");
		var deftness2 = scn.GetFreepsCard("deftness2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var shadowElf = scn.GetShadowCard("elf");
		var shadowAxe = scn.GetShadowCard("axe");
		var shadowDeftness = scn.GetShadowCard("deftness");
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(elf, axe, deftness, cloak, elf2, axe2, elf3, axe3, deftness2);
		scn.MoveCardsToHand(shadowElf, shadowAxe, shadowDeftness);
		assertInHand(elf, axe, deftness, cloak, elf2, axe2, elf3, axe3, deftness2, shadowElf, shadowAxe, shadowDeftness);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, savage2);

		// Thrarin takes part in skirmishes by his own ability.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, troop}, new PhysicalCardImpl[]{thrarin, savage}, new PhysicalCardImpl[]{sam, savage2});

		// An ally is no companion, so not an unbound one: nothing is weakened, and the Shadow player is not asked.
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(5, scn.GetStrength(savage));
		int freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(gandalf));
		scn.FreepsUseCardAction(gandalf);
		scn.FreepsChooseCards(elf, axe, deftness);
		scn.FreepsChooseCard(elf);
		scn.FreepsChooseCard(axe);

		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Thrarin (4, one wound) loses to the Savage and dies; then Sam's skirmish.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);

		// Sam is Ring-bound without being the Ring-bearer: the same again.
		assertEquals(5, scn.GetStrength(savage2));
		freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(gandalf));
		scn.FreepsUseCardAction(gandalf);
		scn.FreepsChooseCards(elf3, axe3, deftness2);
		scn.FreepsChooseCard(elf3);
		scn.FreepsChooseCard(axe3);

		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Sam (3) loses to the Savage (5) and lives; then the Ring-bearer's skirmish.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(frodo);

		// The Ring-bearer is Ring-bound as well: the same again.
		assertEquals(13, scn.GetStrength(troop));
		freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(gandalf));
		// With three cards in hand, all three go unasked; only their order is asked.
		scn.FreepsUseCardAction(gandalf);
		scn.FreepsChooseCard(cloak);
		scn.FreepsChooseCard(elf2);

		assertEquals(freepsDeck + 3, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
