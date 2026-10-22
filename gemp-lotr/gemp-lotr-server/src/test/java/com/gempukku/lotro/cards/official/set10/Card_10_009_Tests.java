package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "10_9");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, no archer; +2 only at sites 6 to 8
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, home site 6; an archery ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("finalcount", "4_69");  // Final Count: an [elven] condition, no Elf; its texts are about Legolas

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits

					// Cards laid on top of the draw deck; only their culture is read.
					put("elfcard", "1_53");     // Lorien Elf: an [elven] companion
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Elrond, Venerable Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot an Elf.<br>At the start of each skirmish involving Elrond, you may reveal the top card of your draw deck. If it is an [elven] card, you may heal another Elf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Venerable Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndAnElfAllyIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gimli = scn.GetFreepsCard("gimli");
		var finalcount = scn.GetFreepsCard("finalcount");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(elrond);

		scn.StartGame();
		assertInHand(elrond);
		// A Dwarf companion and an [elven] card that is no Elf: no Elf to spot.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(elrond));

		scn.MoveAlliesToTable(orophin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(elrond));
		scn.FreepsPlayCard(elrond);

		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithAnElfCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveCardsToHand(elrond);

		scn.StartGame();
		assertInHand(elrond);
		assertTrue(scn.FreepsPlayAvailable(elrond));
		scn.FreepsPlayCard(elrond);

		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfHisSkirmishAnElvenCardRevealedLetsHimHealAnotherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var elfcard = scn.GetFreepsCard("elfcard");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(elrond, haldir, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.AddWoundsToChar(haldir, 2);
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(elrond, 1);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, elfcard);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.MoveMinionsToTable(troop, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elrond, troop},
				new PhysicalCardImpl[]{gimli, soldier}
		);

		// Gimli's skirmish, which does not involve Elrond: nothing is offered.
		scn.FreepsResolveSkirmish(gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Gimli (6) loses to the Soldier (7) and takes a wound.
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsAcceptOptionalTrigger();

		// The top card is shown to both players.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elfcard, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elfcard, false));
		scn.DismissRevealedCards();

		// An [elven] card: another Elf may be healed, an Elf companion or an Elf ally; not Elrond himself, nor
		// the wounded Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, gimli));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(haldir);

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(gimli));
		// Revealed, not moved.
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedAtTheFirstSkirmishItIsOfferedAgainAtTheStartOfTheFierceOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		var elfcard = scn.GetFreepsCard("elfcard");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(elrond, haldir);
		scn.AddWoundsToChar(haldir, 2);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, elfcard);
		assertEquals(2, scn.GetWoundsOn(haldir));

		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, pursuer);
		scn.FreepsResolveSkirmish(elrond);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsDeclineOptionalTrigger();

		// Declined: nothing is shown, nobody is healed.
		assertFalse(scn.BeingShownCards(scn.P1));
		assertFalse(scn.BeingShownCards(scn.P2));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Elrond (8) beats the Pursuer (5), which takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(elrond, pursuer);
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.InFierceRound());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		// Haldir is the one wounded Elf beside Elrond.
		scn.FreepsChooseCard(haldir);

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACardThatIsNotElvenHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elrond, haldir);
		scn.AddWoundsToChar(haldir, 2);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor);
		assertEquals(2, scn.GetWoundsOn(haldir));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, troop);
		scn.FreepsResolveSkirmish(elrond);

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		scn.DismissRevealedCards();

		// A [dwarven] card: no heal is offered.
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheHealMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		var orophin = scn.GetFreepsCard("orophin");
		var elfcard = scn.GetFreepsCard("elfcard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elrond, haldir);
		scn.MoveAlliesToTable(orophin);
		scn.AddWoundsToChar(haldir, 2);
		scn.AddWoundsToChar(orophin, 1);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, elfcard);
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(orophin));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, troop);
		scn.FreepsResolveSkirmish(elrond);

		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, orophin));
		scn.FreepsDeclineChoosing();

		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherElfWoundedNoHealIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		var gimli = scn.GetFreepsCard("gimli");
		var elfcard = scn.GetFreepsCard("elfcard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elrond, haldir, gimli);
		// Elrond himself and a Dwarf are wounded; Haldir, the other Elf, is not.
		scn.AddWoundsToChar(elrond, 1);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, elfcard);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(haldir));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, troop);
		scn.FreepsResolveSkirmish(elrond);

		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnElvenConditionRevealedAlsoLetsHimHealAnotherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		var finalcount = scn.GetFreepsCard("finalcount");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elrond, haldir);
		scn.AddWoundsToChar(haldir, 2);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(finalcount);
		assertSame(finalcount, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, finalcount);
		assertEquals(2, scn.GetWoundsOn(haldir));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, troop);
		scn.FreepsResolveSkirmish(elrond);

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(finalcount, false));
		scn.DismissRevealedCards();

		// An [elven] card that is no companion: the heal is still offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir));
		scn.FreepsChooseCard(haldir);

		assertEquals(1, scn.GetWoundsOn(haldir));
		assertSame(finalcount, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheRevealIsStillOfferedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elrond, haldir);
		scn.AddWoundsToChar(haldir, 2);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetWoundsOn(haldir));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, troop);
		scn.FreepsResolveSkirmish(elrond);

		// As an optional look through an empty draw deck is (Q-0104).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elrond));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
