package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bag", "18_26");
					// Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion, vitality 4, no ability
					put("gandalf", "1_72");
					// Saruman, The White: a [gandalf] Wizard ALLY, vitality 4.  His two abilities are not used.
					put("saruman", "32_24");
					// Ent Avenger: a [gandalf] companion who is an Ent and no Wizard
					put("avenger", "6_27");
					// Drawn to Full Height: a [gandalf] Skirmish spell, cost 0.  Spot a [gandalf] Wizard to make a
					// minion lose all strength bonuses from possessions.
					put("drawn1", "18_19");
					put("drawn2", "18_19");
					// Keep Your Forked Tongue: a [gandalf] Skirmish event that is no spell, cost 1.  Spot Gandalf and
					// 3 twilight tokens to prevent all wounds to a companion who has the Gandalf signet.
					put("tongue", "4_96");
					// Scintillating Bird: a [gandalf] Spell CONDITION, cost 1, to the support area.  Its fellowship
					// ability is not used.
					put("bird", "17_23");
					// Goblin Patrol Troop: a [moria] Orc of vitality 3, no text
					put("troop", "1_177");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl of vitality 2.  Her text reads
					// [wraith] conditions on companions and allies, and there are none.
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RadagastsHerbBagStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Radagast's Herb Bag
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Vitality: +1
		 * Resistance: +2
		 * Game Text: Bearer must be a [gandalf] Wizard.<br>Each time you play a [gandalf] spell, you may exert bearer to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bag");

		assertEquals("Radagast's Herb Bag", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnAGandalfWizardCompanionOrAllyAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToHand(bag);

		scn.StartGame();
		scn.SetTwilight(0);

		assertTrue(scn.FreepsPlayAvailable(bag));
		scn.FreepsPlayCard(bag);

		// A [gandalf] Wizard companion and a [gandalf] Wizard ally.  Not a [gandalf] companion who is no
		// Wizard, nor the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, frodo));
		scn.FreepsChooseCard(saruman);

		assertAttachedTo(bag, saruman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAGandalfSpellExertsBearerToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var drawn = scn.GetFreepsCard("drawn1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, bag);
		scn.MoveCardsToHand(drawn);

		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// Gandalf fights the Troop; Lemenya waits on Frodo, her skirmish not yet begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{frodo, lemenya});
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPlayCard(drawn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bag));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Either minion, the one in this skirmish and the one waiting on Frodo.  No companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, lemenya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsChooseCard(lemenya);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// The spell itself is carried out after.
		scn.FreepsChooseCard(troop);
		assertInDiscard(drawn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWhoBearsItExertsAndTheWizardBesideHimDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var drawn = scn.GetFreepsCard("drawn1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);
		scn.AttachCardsTo(saruman, bag);
		scn.MoveCardsToHand(drawn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		scn.FreepsPlayCard(drawn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bag));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsAcceptOptionalTrigger();

		// The one minion is wounded unasked.  The ally who bears the Bag exerts, not the Wizard in the skirmish.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(drawn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyAndWoundsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var drawn = scn.GetFreepsCard("drawn1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, bag);
		scn.MoveCardsToHand(drawn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		scn.FreepsPlayCard(drawn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bag));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(drawn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void EachSpellPlayedIsAnOfferOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var drawn1 = scn.GetFreepsCard("drawn1");
		var drawn2 = scn.GetFreepsCard("drawn2");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, bag);
		scn.MoveCardsToHand(drawn1, drawn2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		scn.FreepsPlayCard(drawn1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bag));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.ShadowPass();
		scn.FreepsPlayCard(drawn2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bag));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertInDiscard(drawn1, drawn2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AGandalfEventThatIsNoSpellIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var tongue = scn.GetFreepsCard("tongue");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, bag);
		scn.MoveCardsToHand(tongue);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);
		// Keep Your Forked Tongue spots 3 twilight tokens.
		scn.SetTwilight(5);

		assertTrue(scn.FreepsPlayAvailable(tongue));
		scn.FreepsPlayCard(tongue);

		assertInDiscard(tongue);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASpellThatIsAConditionIsAnsweredAsWellAsASpellEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bird = scn.GetFreepsCard("bird");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, bag);
		scn.MoveCardsToHand(bird);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(bird));
		scn.FreepsPlayCard(bird);

		// No minion is in play in the fellowship phase, but the exertion is a cost, so the offer is made all the same.
		assertInZone(Zone.SUPPORT, bird);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bag));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBearerExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bag = scn.GetFreepsCard("bag");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var drawn = scn.GetFreepsCard("drawn1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);
		scn.AttachCardsTo(gandalf, bag);
		scn.MoveCardsToHand(drawn);
		// Vitality 4, +1 for the Bag: four wounds exhaust him.  Saruman, a [gandalf] Wizard who does not bear
		// the Bag, could exert.
		scn.AddWoundsToChar(gandalf, 4);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsExhausted(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(drawn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(drawn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
