package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_014_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haldir", "68_14");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, a maneuver ability never used here
					put("elfinplay", "1_53");     // Lorien Elf: an [elven] Elf companion with no text in play
					put("elfinhand", "1_53");     // a second copy, kept in hand: an [elven] companion card
					put("creation", "1_44");      // Foul Creation: an [elven] Fellowship event (nothing to play in a skirmish)
					put("bow", "1_41");           // Elven Bow: an [elven] possession
					put("armor", "1_8");          // Dwarven Armor: a [dwarven] possession, the card in hand that is not [elven]

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else
					put("party", "1_158");        // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HaldirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Haldir, Warrior Messenger
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * 	<b>Skirmish:</b> Exert Haldir or discard a [elven] card from hand to make a minion skirmishing Haldir strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haldir");

		assertEquals("Haldir", card.getBlueprint().getTitle());
		assertEquals("Warrior Messenger", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void HaldirIsPlayedFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCardsToHand(haldir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(haldir));
		scn.FreepsPlayCard(haldir);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingHaldirMakesAMinionSkirmishingHimStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elfinplay = scn.GetFreepsCard("elfinplay");
		var creation = scn.GetFreepsCard("creation");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(haldir, aragorn, elfinplay);
		scn.MoveCardsToHand(creation, bow, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, troop}, new PhysicalCardImpl[]{aragorn, party});
		// The Savage, left over, is the Shadow player's to assign: Haldir faces two minions.
		scn.ShadowAssignToMinions(haldir, savage);
		scn.FreepsResolveSkirmish(haldir);

		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(haldir));
		scn.FreepsUseCardAction(haldir);

		// Both costs can be paid, so the player is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Exert Haldir"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		scn.FreepsChooseOption("Exert Haldir");

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(elfinplay));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetFreepsHandCount());

		// The Raiding Party is assigned to Aragorn, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(party));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		assertEquals(11, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(9, scn.GetStrength(party));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DiscardingAnElvenCardFromHandMakesAMinionSkirmishingHaldirStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elfinplay = scn.GetFreepsCard("elfinplay");
		var elfinhand = scn.GetFreepsCard("elfinhand");
		var creation = scn.GetFreepsCard("creation");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(haldir, aragorn, elfinplay);
		scn.MoveCardsToHand(creation, bow, elfinhand, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, troop}, new PhysicalCardImpl[]{aragorn, party});
		scn.ShadowAssignToMinions(haldir, savage);
		scn.FreepsResolveSkirmish(haldir);

		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsUseCardAction(haldir);
		assertTrue(scn.FreepsChoiceAvailable("Exert Haldir"));
		scn.FreepsChooseOption("Discard");

		// An event, a possession and a companion, all [elven] and all in hand. Not the [dwarven] card in hand,
		// and not an [elven] card in play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(creation, bow, elfinhand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, elfinplay, haldir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(bow);

		assertInDiscard(bow);
		assertInHand(creation, elfinhand, armor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(haldir));

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(party));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(3, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(party));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoElvenCardInHandHaldirExertsWithoutBeingAskedUntilHeIsExhaustedAndThenHasNoAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var elfinplay = scn.GetFreepsCard("elfinplay");
		var creation = scn.GetFreepsCard("creation");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(haldir, elfinplay);
		// The only [elven] cards are in play and in the discard pile.
		scn.MoveCardsToHand(armor);
		scn.MoveCardsToDiscard(creation);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haldir, troop);
		scn.FreepsResolveSkirmish(haldir);

		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(haldir));
		scn.FreepsUseCardAction(haldir);

		// One cost that can be paid and one minion: nothing is asked.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		scn.ShadowPass();

		scn.FreepsUseCardAction(haldir);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(9, scn.GetStrength(troop));
		scn.ShadowPass();

		// Vitality 3 with 2 wounds: exhausted, and still no [elven] card in hand.
		assertFalse(scn.FreepsActionAvailable(haldir));
		assertInHand(armor);
		assertInDiscard(creation);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedHaldirDiscardsHisOnlyElvenCardWithoutBeingAskedAndThenHasNoAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var creation = scn.GetFreepsCard("creation");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveCardsToHand(creation, armor);
		scn.AddWoundsToChar(haldir, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haldir, troop);
		scn.FreepsResolveSkirmish(haldir);

		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(haldir));
		scn.FreepsUseCardAction(haldir);

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInDiscard(creation);
		assertInHand(armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(11, scn.GetStrength(troop));
		scn.ShadowPass();

		assertFalse(scn.FreepsActionAvailable(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthLossLastsUntilTheEndOfHaldirsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(haldir, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, troop}, new PhysicalCardImpl[]{aragorn, party});
		scn.FreepsResolveSkirmish(haldir);

		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsUseCardAction(haldir);
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(1, scn.GetWoundsOn(haldir));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(troop));
		scn.FreepsPass();

		// Haldir's 6 loses to 11 without being overwhelmed: one wound on top of his exertion, and the Troop lives.
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedDuringAnotherCompanionsSkirmishHaldirExertsAndNoMinionIsWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(haldir, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, troop}, new PhysicalCardImpl[]{aragorn, party});
		scn.FreepsResolveSkirmish(aragorn);

		// The Troop is assigned to Haldir and waiting; the Raiding Party is skirmishing Aragorn. Neither is
		// skirmishing Haldir, but his cost can be paid, so the action is there to be used for nothing.
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.FreepsActionAvailable(haldir));
		scn.FreepsUseCardAction(haldir);

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(party));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
