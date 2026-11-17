package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import com.gempukku.lotro.game.PhysicalCardImpl;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;

import static org.junit.Assert.*;

public class Card_15_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spied1", "15_24");
					put("spied2", "15_24");
					put("elf", "1_53");          // Lorien Elf: [elven] Elf companion with no keyword, strength 4 (cheated in past "To play, spot an Elf")
					put("legolas", "1_51");      // Legolas, Prince of Mirkwood: [elven] archer Elf companion, strength 6; +3 only against a Nazgul
					put("celeborn", "1_34");     // Celeborn, Lord of Lorien: [elven] Elf ally who is no archer
					put("garrison", "8_36");     // Garrison of Gondor: [gondor] Man archer companion (cheated in past "spot 3 [gondor] companions")
					put("orophin", "1_56");      // Orophin, Lorien Bowman: [elven] archer Elf ally; an Archery ability used only on purpose
					put("bowman", "2_60");       // Goblin Bowman: an archer minion with no other text
					put("uruviel", "15_26");     // Uruviel, Woodland Maid: [elven] follower; its texts act only on a transfer
					put("woodhall", "18_17");    // Woodhall Elf, Exile: [elven] follower; its text acts only on a transfer
					put("pallando", "13_37");    // Pallando, Far-travelling One: [gandalf] follower; its texts act only borne
					put("spear", "1_182");       // Goblin Spear: [moria] possession, twilight cost 1
					put("banner", "6_98");       // Banner of the Eye: [sauron] possession, twilight cost 0
					put("ithil", "9_47");        // Ithil Stone: [sauron] artifact (a Shadow item that is no possession), twilight cost 0
					put("coming", "1_196");      // They Are Coming: [moria] condition, twilight cost 3
					put("bow", "1_41");          // Elven Bow: a Free Peoples possession, twilight cost 1
					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpiedFromAfarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Spied From Afar
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 3 Elves.<br>While you can spot an [elven] follower, the twilight cost of each Shadow possession is +1.<br><b>Skirmish:</b> Discard this condition to make an [elven] companion strength +1 for each archer companion you can spot.
		*/

		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");

		assertEquals("Spied From Afar", spied.getBlueprint().getTitle());
		assertNull(spied.getBlueprint().getSubtitle());
		assertFalse(spied.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, spied.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, spied.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, spied.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(spied, Keyword.SUPPORT_AREA));
		assertEquals(2, spied.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnceThreeElvesCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var elf = scn.GetFreepsCard("elf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var legolas = scn.GetFreepsCard("legolas");
		var uruviel = scn.GetFreepsCard("uruviel");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(celeborn);
		// Uruviel is an [elven] follower: no race, so no Elf.
		scn.MoveCardsToSupportArea(uruviel);
		scn.MoveCardsToHand(spied, legolas);
		assertInHand(spied, legolas);
		assertInZone(Zone.SUPPORT, uruviel);

		scn.StartGame();

		// Two Elves, a companion and an ally.
		assertFalse(scn.FreepsPlayAvailable(spied));

		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsPlayAvailable(spied));
		scn.FreepsPlayCard(spied);
		assertInZone(Zone.SUPPORT, spied);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileAnElvenFollowerCanBeSpottedEachShadowPossessionCostsOneMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var elf = scn.GetFreepsCard("elf");
		var uruviel = scn.GetFreepsCard("uruviel");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(spied);
		scn.MoveCardsToHand(uruviel, bow);
		assertInZone(Zone.SUPPORT, spied);
		assertInHand(uruviel, bow);

		var spear = scn.GetShadowCard("spear");
		var banner = scn.GetShadowCard("banner");
		var ithil = scn.GetShadowCard("ithil");
		var coming = scn.GetShadowCard("coming");

		scn.StartGame();
		scn.SetShadowHand(spear, banner, ithil, coming);
		scn.FreepsRefreshDecision();
		assertInHand(spear, banner, ithil, coming);

		// No [elven] follower yet: the printed costs.
		assertEquals(1, scn.GetTwilightCost(spear));
		assertEquals(0, scn.GetTwilightCost(banner));
		assertEquals(0, scn.GetTwilightCost(ithil));
		assertEquals(3, scn.GetTwilightCost(coming));
		assertEquals(1, scn.GetTwilightCost(bow));

		scn.FreepsPlayCard(uruviel);
		assertInZone(Zone.SUPPORT, uruviel);

		// The two Shadow possessions, of two cultures, are +1; the Shadow artifact, the Shadow condition and the
		// Free Peoples possession are not.
		assertEquals(2, scn.GetTwilightCost(spear));
		assertEquals(1, scn.GetTwilightCost(banner));
		assertEquals(0, scn.GetTwilightCost(ithil));
		assertEquals(3, scn.GetTwilightCost(coming));
		assertEquals(1, scn.GetTwilightCost(bow));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFollowerOfAnotherCultureDoesNotRaiseTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var elf = scn.GetFreepsCard("elf");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(spied, pallando);
		assertInZone(Zone.SUPPORT, spied, pallando);

		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		scn.SetShadowHand(spear);
		scn.FreepsRefreshDecision();
		assertInHand(spear);

		// A [gandalf] follower, and an [elven] card that is no follower (Lorien Elf).
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetTwilightCost(spear));
	}

	@Test
	public void AnElvenFollowerBorneByACompanionRaisesTheCostToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var elf = scn.GetFreepsCard("elf");
		var woodhall = scn.GetFreepsCard("woodhall");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(spied);
		scn.AttachCardsTo(elf, woodhall);
		assertInZone(Zone.SUPPORT, spied);
		assertAttachedTo(woodhall, elf);

		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		scn.SetShadowHand(spear);
		scn.FreepsRefreshDecision();
		assertInHand(spear);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetTwilightCost(spear));
	}

	@Test
	public void SkirmishAbilityDiscardsThisConditionToMakeAnElvenCompanionPlusOneForEachArcherCompanionUntilTheEndOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied1 = scn.GetFreepsCard("spied1");
		var spied2 = scn.GetFreepsCard("spied2");
		var frodo = scn.GetRingBearer();
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var celeborn = scn.GetFreepsCard("celeborn");
		var garrison = scn.GetFreepsCard("garrison");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(spied1, spied2);
		assertInZone(Zone.SUPPORT, spied1, spied2);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var bowman = scn.GetShadowCard("bowman");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner1}, new PhysicalCardImpl[]{elf, runner2});
		// The archers come down once the archery phase is past ("Archers get in the way"): two archer
		// companions (Legolas, unassigned, and the [gondor] Garrison), an archer ally and an archer minion.
		scn.MoveCompanionsToTable(legolas, garrison);
		scn.MoveAlliesToTable(orophin);
		scn.MoveMinionsToTable(bowman);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(elf));
		assertTrue(scn.FreepsActionAvailable(spied1));
		scn.FreepsUseCardAction(spied1);
		assertInDiscard(spied1);
		assertInZone(Zone.SUPPORT, spied2);

		// "An [elven] companion": the Elf of a later skirmish and the unassigned Legolas; not the [elven] ally,
		// nor the [gondor] or [shire] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, garrison, frodo));
		scn.FreepsChooseCard(elf);

		// 4 + 2: Legolas and the Garrison; Orophin is an ally and the Bowman a minion.
		assertEquals(6, scn.GetStrength(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Frodo's skirmish resolves; in the Elf's own skirmish the bonus is gone.
		scn.PassSkirmishActions();
		// Frodo loses to the Runner: The Ruling Ring's offer on his wound is declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(elf));
	}

	@Test
	public void WithOneArcherCompanionTheBonusIsPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(spied);
		assertInZone(Zone.SUPPORT, spied);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, runner1);
		scn.MoveCompanionsToTable(legolas);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(4, scn.GetStrength(elf));
		scn.FreepsUseCardAction(spied);
		scn.FreepsChooseCard(elf);

		// 4 + 1: Legolas alone.
		assertInDiscard(spied);
		assertEquals(5, scn.GetStrength(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoArcherCompanionTheConditionIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var elf = scn.GetFreepsCard("elf");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(spied);
		assertInZone(Zone.SUPPORT, spied);

		var runner1 = scn.GetShadowCard("runner1");
		var bowman = scn.GetShadowCard("bowman");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, runner1);
		// Archers, but no archer companion.
		scn.MoveAlliesToTable(orophin);
		scn.MoveMinionsToTable(bowman);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(4, scn.GetStrength(elf));
		assertTrue(scn.FreepsActionAvailable(spied));
		scn.FreepsUseCardAction(spied);
		// The Elf is the one [elven] companion and is chosen unasked; the count is 0.
		assertInDiscard(spied);
		assertEquals(4, scn.GetStrength(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoElvenCompanionTheAbilityIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var frodo = scn.GetRingBearer();
		var garrison = scn.GetFreepsCard("garrison");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(spied);
		assertInZone(Zone.SUPPORT, spied);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner1);
		// The archer Garrison comes down past the archery phase, unassigned.
		scn.MoveCompanionsToTable(garrison);
		scn.FreepsResolveSkirmish(frodo);

		// An archer companion to count, and nobody to strengthen: Celeborn is an ally, the Garrison [gondor],
		// Frodo [shire].
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(garrison));
		assertEquals(6, scn.GetStrength(celeborn));
		assertTrue(scn.FreepsActionAvailable(spied));
		scn.FreepsUseCardAction(spied);

		assertInDiscard(spied);
		assertEquals(8, scn.GetStrength(garrison));
		assertEquals(6, scn.GetStrength(celeborn));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spied = scn.GetFreepsCard("spied1");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToSupportArea(spied);
		assertInZone(Zone.SUPPORT, spied);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(spied));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(elf, runner1);
		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(spied));
	}
}
