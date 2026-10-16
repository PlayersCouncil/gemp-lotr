package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_07_230_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "7_230");
					put("eowyn", "7_228");      // Eowyn, Dernhelm: [rohan] companion; +2 only against a mounted or a fierce minion, and none is laid
					put("theoden", "4_292");    // Theoden, Son of Thengel: a [rohan] Man companion who is no Eowyn; a regroup ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("wildmen", "4_4");      // Band of Wild Men: [dunland] Man, strength 11, vitality 2; a trigger on winning a skirmish only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a trigger on being played only (cheated in)
					put("rally", "11_160");     // War Now Calls Us: [rohan] Skirmish event, cost 0; exert a [rohan] companion to make him or her strength +3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Éowyn's Sword, Dernhelm's Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +3
		 * Game Text: Bearer must be Éowyn.
		 * You may play this possession any time you could play a skirmish event.
		 * When you play this possession, you may wound a minion Éowyn is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Éowyn's Sword", card.getBlueprint().getTitle());
		assertEquals("Dernhelm's Blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnEowynInHerSkirmishItMayWoundAMinionSheIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var eowyn = scn.GetFreepsCard("eowyn");
		var theoden = scn.GetFreepsCard("theoden");
		var troop = scn.GetShadowCard("troop");
		var wildmen = scn.GetShadowCard("wildmen");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(eowyn, theoden);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, wildmen, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { eowyn, troop }, new PhysicalCardImpl[] { theoden, runner });
		// The minion left over goes to Eowyn too: she is skirmishing two.
		scn.ShadowAssignToMinions(eowyn, wildmen);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(wildmen));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(sword);
		assertInZone(Zone.ATTACHED, sword);
		assertSame(eowyn, sword.getAttachedTo());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sword));

		scn.FreepsAcceptOptionalTrigger();
		// The minion assigned to Theoden, whose skirmish has not begun, is not one she is skirmishing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, wildmen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(wildmen);

		assertEquals(1, scn.GetWoundsOn(wildmen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedInAnotherCompanionsSkirmishItGoesToEowynAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var eowyn = scn.GetFreepsCard("eowyn");
		var theoden = scn.GetFreepsCard("theoden");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(eowyn, theoden);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { eowyn, troop }, new PhysicalCardImpl[] { theoden, runner });
		scn.FreepsResolveSkirmish(theoden);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(sword);
		assertSame(eowyn, sword.getAttachedTo());
		// The Goblin Runner is skirmishing Theoden, and the Troop is assigned to Eowyn with her skirmish still to
		// come: neither is a minion she is skirmishing, so there is nothing to offer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheManeuverPhaseAndTheWoundMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var eowyn = scn.GetFreepsCard("eowyn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sword));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, troop);
		scn.FreepsResolveSkirmish(eowyn);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(sword);
		assertSame(eowyn, sword.getAttachedTo());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sword));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWithoutEowyn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var theoden = scn.GetFreepsCard("theoden");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(theoden);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, troop);
		scn.FreepsResolveSkirmish(theoden);

		// A [rohan] Man companion in a skirmish, and no Eowyn to bear it.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sword));
	}

	@Test
	public void AlreadyBorneItWoundsNobodyWhenHerSkirmishBeginsNorWhenAnotherCardIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rally = scn.GetFreepsCard("rally");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, sword);
		scn.MoveCardsToHand(rally);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, troop);
		scn.FreepsResolveSkirmish(eowyn);

		// The wound is offered as the sword is played, not while it is borne in her skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Nor is it offered when some other card is played in that skirmish, the sword being in play.
		assertEquals(0, scn.GetWoundsOn(eowyn));
		scn.FreepsPlayCard(rally);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertInZone(Zone.DISCARD, rally);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sword));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
