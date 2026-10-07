package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_015_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helm", "51_15");

					put("gimli", "1_12");         // Gimli, Dwarf of Erebor: strength 6, vitality 3; a fellowship ability only
					put("gimli2", "1_13");        // Gimli, Son of Gloin: the other Gimli of set 1
					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2
					put("sam", "1_311");          // Sam, Son of Hamfast: strength 3, vitality 4

					put("berserker", "3_66");     // Orthanc Berserker: strength 11, vitality 3, damage +1; its maneuver ability needs 5 burdens
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1
					put("marksman1", "1_176");    // Goblin Marksman: archer and nothing else
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimlisHelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gimli's Helm
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Helm
		 * Game Text: Bearer must be Gimli.
		 * 	He takes no more than 1 wound during each skirmish phase.
		 * 	<b>Skirmish:</b> Discard Gimli's Helm to make Gimli unable to take wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helm");

		assertEquals("Gimli's Helm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HELM));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGimliIsInPlayAndThenGoesOnHimForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		// A Dwarf companion who is not Gimli, and Gimli still in hand.
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(helm, gimli);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(helm));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(helm));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(helm);
		assertEquals(twilight, scn.GetTwilight());
		assertAttachedTo(helm, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EitherGimliIsOfferedAsBearerAndNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var gimli2 = scn.GetFreepsCard("gimli2");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		// Gimli is unique: only cheating puts two of him on one table, which is done here to see the choice.
		scn.MoveCompanionsToTable(gimli, gimli2, guard);
		scn.MoveCardsToHand(helm);

		scn.StartGame();

		scn.FreepsPlayCard(helm);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, gimli2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, frodo));

		scn.FreepsChooseCard(gimli2);
		assertAttachedTo(helm, gimli2);
		assertNotAttachedTo(helm, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerTakesOneWoundFromADamagePlusOneMinionInEachSkirmishPhaseWhileAnotherCompanionTakesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, sam);
		scn.AttachCardsTo(gimli, helm);

		scn.StartGame();

		var berserker = scn.GetShadowCard("berserker");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(berserker, savage);
		// Fierce, so that Gimli fights the Berserker in a second skirmish phase of the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, berserker, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, berserker},
				new PhysicalCardImpl[]{sam, savage});

		// Sam, strength 3, loses to the Savage, strength 5 and damage +1: two wounds.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli, strength 6, loses to the Berserker, strength 11 and damage +1: one wound of the two.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertAttachedTo(helm, gimli);

		// The fierce skirmish is a skirmish phase of its own: one wound more.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, berserker);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerTakesBothArcheryWoundsSinceTheArcheryPhaseIsNoSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, helm);

		scn.StartGame();

		scn.MoveMinionsToTable(scn.GetShadowCard("marksman1"), scn.GetShadowCard("marksman2"));

		// Two archers: two archery wounds for the Free Peoples player to place.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo));
		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(helm, gimli);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void BearerTakesOneThreatWoundInAnotherCompanionsSkirmishPhaseAndNoSecondThenOneMoreInHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, guard, sam);
		scn.AttachCardsTo(gimli, helm);

		scn.StartGame();

		var berserker = scn.GetShadowCard("berserker");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(berserker, savage);
		scn.AddThreats(2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, savage},
				new PhysicalCardImpl[]{gimli, berserker});
		scn.FreepsResolveSkirmish(guard);
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertEquals(2, scn.GetThreats());
		scn.PassSkirmishActions();

		// The Guard, strength 4 and vitality 2, loses to the Savage, strength 5 and damage +1, and is killed by
		// the two wounds: the two threats come off as two wounds for the Free Peoples player to assign, in the
		// Guard's skirmish phase.
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, sam, frodo));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Gimli has taken his one wound of this skirmish phase; a second cannot be assigned to him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));

		// Gimli's own skirmish is the next skirmish phase, with no other phase in between: he loses to the
		// Berserker, strength 11 and damage +1, and takes one wound of the two.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, berserker));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(helm, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsTheHelmAndGimliTakesNoWoundUntilThatSkirmishPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, helm);

		scn.StartGame();

		var berserker = scn.GetShadowCard("berserker");
		scn.MoveMinionsToTable(berserker);
		// Fierce, so that Gimli fights the Berserker in a second skirmish phase of the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, berserker, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, berserker);
		assertAttachedTo(helm, gimli);
		assertTrue(scn.FreepsActionAvailable(helm));

		scn.FreepsUseCardAction(helm);
		assertInDiscard(helm);
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli, strength 6, loses to the Berserker, strength 11 and damage +1, and takes neither wound.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, berserker);

		// The next skirmish phase: he can be wounded again, and with the Helm gone he takes both wounds.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, berserker);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedInAnySkirmishPhaseAndNotInTheAssignmentOrRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, sam);
		scn.AttachCardsTo(gimli, helm);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(helm));

		// Sam's skirmish: Gimli is in none.
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(sam, savage);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.FreepsActionAvailable(helm));

		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(helm));
		assertAttachedTo(helm, gimli);
	}

	@Test
	public void AfterTheAbilityGimliCannotBeAssignedAThreatWoundAndTheOtherCompanionsCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, guard, sam);
		scn.AttachCardsTo(gimli, helm);

		scn.StartGame();

		var berserker = scn.GetShadowCard("berserker");
		scn.MoveMinionsToTable(berserker);
		scn.AddThreats(1);

		// The Guard's skirmish; Gimli is in none.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, berserker);
		assertTrue(scn.FreepsActionAvailable(helm));
		scn.FreepsUseCardAction(helm);
		assertInDiscard(helm);
		assertEquals(1, scn.GetThreats());

		// The Guard, strength 4, is overwhelmed by the Berserker, strength 11, and killed: the threat comes off
		// the dead pile as a wound to assign. Gimli has taken no wound this phase and still cannot be given it.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));

		scn.FreepsChooseCard(sam);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
