package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("artifice", "17_4");

					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion, strength 5, no damage bonus; +2 only against an Orc
					// Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; "Skirmish: Exert Gimli to make him
					// strength +2", used only on purpose.
					put("gimli", "1_13");
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability only

					// The set 9 Dwarven Rings: unique [dwarven] artifacts, item class Ring.  Their abilities are used
					// only on purpose.
					put("accretion", "9_6");      // Ring of Accretion
					put("fury", "9_7");           // Ring of Fury
					put("guile", "9_8");          // Ring of Guile: "Maneuver: Take a [dwarven] event into hand from your discard pile. Discard this artifact."
					put("retribution", "9_9");    // Ring of Retribution

					put("vilya", "9_23");         // Vilya, Ring of Air: an [elven] artifact
					put("dwarvenaxe", "1_9");     // Dwarven Axe: a [dwarven] possession, hand weapon
					put("handaxe", "2_10");       // Hand Axe: a [dwarven] possession, hand weapon
					put("guard", "1_7");          // Dwarf Guard: a [dwarven] companion
					put("stout", "4_57");         // Stout and Strong: a [dwarven] condition
					put("axestrike", "1_3");      // Axe Strike: a [dwarven] Skirmish event

					put("savage", "1_151");       // Uruk Savage: a minion for the maneuver phase; no Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofArtificeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Ring of Artifice
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Game Text: Bearer must be a Dwarf. Bearer is damage +1.
		 * Bearer is strength +1 for each [dwarven] artifact in your discard pile.
		 * Regroup: Discard this to take a non-event [dwarven] card into hand from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("artifice");

		assertEquals("Ring of Artifice", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnADwarfCompanionOrAllyAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artifice = scn.GetFreepsCard("artifice");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(artifice);

		scn.StartGame();
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));

		scn.FreepsPlayCard(artifice);
		// A Dwarf companion and a Dwarf ally; not Aragorn or Frodo, companions who are no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);
		assertAttachedTo(artifice, farin);
		assertTrue(scn.HasKeyword(farin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		// The other Dwarf, who does not bear it, has no damage bonus.
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus1ForEachDwarvenArtifactInYourDiscardPileAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artifice = scn.GetFreepsCard("artifice");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var guile = scn.GetFreepsCard("guile");
		var fury = scn.GetFreepsCard("fury");
		var retribution = scn.GetFreepsCard("retribution");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		var vilya = scn.GetFreepsCard("vilya");
		var accretion = scn.GetFreepsCard("accretion");
		var shadowAccretion = scn.GetShadowCard("accretion");
		scn.MoveCompanionsToTable(farin, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(farin, artifice);
		// A [dwarven] artifact in play, borne by another Dwarf.
		scn.AttachCardsTo(grimir, guile);
		// Two [dwarven] artifacts in the pile, beside a [dwarven] possession and an [elven] artifact.
		scn.MoveCardsToDiscard(fury, retribution, dwarvenaxe, vilya);
		// A [dwarven] artifact in hand, and one in the opponent's discard pile.
		scn.MoveCardsToHand(accretion);
		scn.MoveCardsToDiscard(shadowAccretion);
		assertAttachedTo(artifice, farin);
		assertAttachedTo(guile, grimir);
		assertInZone(Zone.DISCARD, fury, retribution, dwarvenaxe, vilya, shadowAccretion);
		assertInZone(Zone.HAND, accretion);

		scn.StartGame();
		// 5 + 1 for Ring of Fury + 1 for Ring of Retribution
		assertEquals(7, scn.GetStrength(farin));
		// Another Dwarf, who does not bear it.
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarvenArtifactDiscardedByLegalPlayMakesBearerStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artifice = scn.GetFreepsCard("artifice");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var guile = scn.GetFreepsCard("guile");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(farin, gimli);
		scn.AttachCardsTo(farin, artifice);
		scn.AttachCardsTo(gimli, guile);
		// A [dwarven] event for Ring of Guile to take, so that its ability does something.
		scn.MoveCardsToDiscard(axestrike);
		assertAttachedTo(artifice, farin);
		assertAttachedTo(guile, gimli);
		assertInZone(Zone.DISCARD, axestrike);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.MANEUVER);
		// No [dwarven] artifact in the pile: Farin's printed 5.
		assertEquals(5, scn.GetStrength(farin));

		scn.FreepsUseCardAction(guile);
		assertInZone(Zone.HAND, axestrike);
		assertInZone(Zone.DISCARD, guile);
		// 5 + 1 for Ring of Guile, now in the pile
		assertEquals(6, scn.GetStrength(farin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItToTakeANonEventDwarvenCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artifice = scn.GetFreepsCard("artifice");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		var fury = scn.GetFreepsCard("fury");
		var stout = scn.GetFreepsCard("stout");
		var axestrike = scn.GetFreepsCard("axestrike");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowHandaxe = scn.GetShadowCard("handaxe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, artifice);
		// Non-event [dwarven] cards of four types: a companion, a possession, an artifact and a condition.
		// Beside them a [dwarven] event and a [gondor] companion, and in the opponent's pile a [dwarven]
		// possession of a title the Free Peoples pile does not hold.
		scn.MoveCardsToDiscard(guard, dwarvenaxe, fury, stout, axestrike, aragorn, shadowHandaxe);
		assertAttachedTo(artifice, gimli);
		assertInZone(Zone.DISCARD, guard, dwarvenaxe, fury, stout, axestrike, aragorn, shadowHandaxe);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(artifice));

		scn.FreepsUseCardAction(artifice);
		// The cost: the Ring is discarded from Gimli.
		assertInZone(Zone.DISCARD, artifice);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, dwarvenaxe, fury, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axestrike, aragorn, shadowHandaxe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(dwarvenaxe);
		assertInZone(Zone.HAND, dwarvenaxe);
		assertInZone(Zone.DISCARD, guard, fury, stout, axestrike, aragorn, shadowHandaxe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artifice = scn.GetFreepsCard("artifice");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, artifice);
		scn.MoveCardsToDiscard(dwarvenaxe);
		assertAttachedTo(artifice, gimli);
		assertInZone(Zone.DISCARD, dwarvenaxe);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(artifice));
	}

	@Test
	public void TheRingDiscardedForTheCostIsNotOfferedAsTheCardTakenBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artifice = scn.GetFreepsCard("artifice");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarvenaxe = scn.GetFreepsCard("dwarvenaxe");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, artifice);
		scn.MoveCardsToDiscard(dwarvenaxe, fury);
		assertAttachedTo(artifice, gimli);
		assertInZone(Zone.DISCARD, dwarvenaxe, fury);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(artifice);
		assertInZone(Zone.DISCARD, artifice);
		// The Ring is itself a non-event [dwarven] card, now in the pile, but a card discarded to pay for a
		// retrieval is not the card retrieved (Ketura, Q-0192; from play as from hand, F-0577).
		assertTrue(scn.FreepsHasCardChoiceAvailable(dwarvenaxe, fury));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(artifice));

		scn.FreepsChooseCard(fury);
		assertInZone(Zone.HAND, fury);
		assertInZone(Zone.DISCARD, artifice, dwarvenaxe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherNonEventDwarvenCardInThePileTheRingIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var artifice = scn.GetFreepsCard("artifice");
		var gimli = scn.GetFreepsCard("gimli");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, artifice);
		// Only a [dwarven] event in the pile.
		scn.MoveCardsToDiscard(axestrike);
		assertAttachedTo(artifice, gimli);
		assertInZone(Zone.DISCARD, axestrike);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		// The ability has a cost, so it is offered whatever its effect can do (Q-0034).
		assertTrue(scn.FreepsActionAvailable(artifice));
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(artifice);
		assertInZone(Zone.DISCARD, artifice, axestrike);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
