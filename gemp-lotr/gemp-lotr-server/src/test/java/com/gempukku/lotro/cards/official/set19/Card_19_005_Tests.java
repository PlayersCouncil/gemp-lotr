package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "19_5");
					put("army2", "19_5");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6, vitality 3; +3 against a Nazgul only
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: [elven] Elf companion, strength 5; +2 at sites 6 to 8 only
					put("lorienelf", "1_53");   // Lorien Elf: [elven] Elf companion, strength 4, vitality 2; a play requirement only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally; heals allies of site 3 at the start of the turn, a fellowship ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability never used

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("seeker", "4_195");     // Uruk Seeker: fierce, strength 6, vitality 2; its bonus is against a bearer of a search card only
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmyLongTrainedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Army Long Trained
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Strength: +1
		 * Game Text: Bearer must be an [elven] companion.
		 * 	Each time bearer wins a skirmish, you may transfer this condition to another [elven] companion.
		 * 	At the start of the archery phase, you must exert bearer or return this condition to hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Army Long Trained", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnlyOnAnElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(army);
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(army));
		scn.FreepsPlayCard(army);
		// Each [elven] companion may bear it; an [elven] ally, a [gondor] companion and the Ring-bearer may not.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, aragorn, frodo));

		scn.FreepsChooseCard(haldir);
		assertAttachedTo(army, haldir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishItMayBeTransferredToAnotherElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var army2 = scn.GetFreepsCard("army2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienElf = scn.GetFreepsCard("lorienelf");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(arwen, haldir, lorienElf, aragorn);
		scn.MoveAlliesToTable(elrond);
		// A second copy on an [elven] companion who fights no skirmish.
		scn.AttachCardsTo(arwen, army);
		scn.AttachCardsTo(haldir, army2);
		assertAttachedTo(army, arwen);
		assertAttachedTo(army2, haldir);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, seeker);

		// At the start of the archery phase each copy asks for its bearer's exertion; both bearers exert.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsChoose("Exert bearer");
		scn.FreepsChoose("Exert bearer");
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, savage }, new PhysicalCardImpl[] { arwen, seeker });

		// Aragorn (8), who bears no Army Long Trained, beats Uruk Savage (5): nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Arwen (6 + 1) beats Uruk Seeker (6), who lives through it: only the copy she bears is offered.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(army));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(army2));

		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(army, arwen);
		assertAttachedTo(army2, haldir);

		// The Seeker is fierce: Arwen beats him a second time, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(arwen, seeker);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(army));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(army2));

		scn.FreepsAcceptOptionalTrigger();
		// Another [elven] companion: not the bearer, not the [elven] ally, not the [gondor] companion, not a minion.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, lorienElf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, elrond, aragorn, savage));

		scn.FreepsChooseCard(lorienElf);
		assertAttachedTo(army, lorienElf);
		assertAttachedTo(army2, haldir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheBearerIsTheOnlyElvenCompanionTheTransferIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		// An [elven] ally and a [gondor] companion beside her, neither of whom may take it.
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(arwen, army);
		assertAttachedTo(army, arwen);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsChoose("Exert bearer");
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();

		// Arwen (6 + 1) beats Uruk Savage (5).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(army, arwen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, army);
		assertAttachedTo(army, arwen);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsChoose("Exert bearer");
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, troop);
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		// Goblin Patrol Troop (13) beats Arwen (6 + 1) without overwhelming her.
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertAttachedTo(army, arwen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheStartOfTheArcheryPhaseBearerMayBeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, army);
		assertAttachedTo(army, arwen);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		// The start of the maneuver phase asks nothing.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertAttachedTo(army, arwen);

		scn.PassManeuverActions();
		// The start of the archery phase: exert her or return it, both possible.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsGetMultipleChoices().contains("Exert bearer"));
		assertTrue(scn.FreepsGetMultipleChoices().contains("Return this condition to hand"));

		scn.FreepsChoose("Exert bearer");
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertAttachedTo(army, arwen);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtTheStartOfTheArcheryPhaseItMayBeReturnedToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, army);
		assertAttachedTo(army, arwen);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsChoose("Return this condition to hand");

		assertInHand(army);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithItsBearerExhaustedItReturnsToHandUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var army2 = scn.GetFreepsCard("army2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.AttachCardsTo(arwen, army);
		scn.AttachCardsTo(haldir, army2);
		// Arwen, vitality 3, exhausted; Haldir unwounded.
		scn.AddWoundsToChar(arwen, 2);
		assertAttachedTo(army, arwen);
		assertAttachedTo(army2, haldir);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ARCHERY);
		// Arwen cannot exert, so her copy goes back to hand with no question; Haldir's copy asks its own.
		assertInHand(army);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());

		scn.FreepsChoose("Exert bearer");
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertAttachedTo(army2, haldir);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
