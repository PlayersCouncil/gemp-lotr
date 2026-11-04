package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("food", "18_36");
					put("smeagol", "6_45");         // Smeagol, Poor Creature: [gollum] companion; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] companion; an assignment ability used only on purpose
					put("deagol", "13_46");         // Deagol, Fateful Finder: [gollum] Free Peoples follower who is no Smeagol
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: the same creature under another title; a regroup ability only
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] companion, strength 8; a maneuver ability used only on purpose
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5
					put("drums", "1_168");          // Drums in the Deep: Shadow skirmish event, make a [moria] Orc strength +2
					put("swordarm", "1_116");       // Swordarm of the White Tower: Free Peoples skirmish event, make a [gondor] companion strength +2
					// Flanking Attack: [raider] event, cost 0. "Maneuver or Skirmish: Exert a Southron to add (3)."
					put("flanking", "4_234");
					put("lancers", "4_217");        // Desert Lancers: [raider] Southron, strength 11, vitality 3; a regroup ability only
					put("haradrim", "5_70");        // Army of Haradrim: Southron, strength 14; "Skirmish: Remove (3) to play a [raider] mount from your discard pile"
					put("mumak", "5_78");           // War Mumak: [raider] mount possession, played on the Army in the skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TimeforFoodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Time for Food
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a skirmish event is played, spot Sméagol to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("food");

		assertEquals("Time for Food", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithSmeagolSpottedAShadowSkirmishEventIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var food = scn.GetFreepsCard("food");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(food);
		scn.MoveCardsToHand(drums);
		assertInHand(food, drums);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(drums);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(food));
		scn.FreepsAcceptOptionalTrigger();

		// Cancelled: the Orc is not made +2, and the event goes to its owner's discard pile.
		assertEquals(5, scn.GetStrength(runner));
		assertInDiscard(drums, food);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheShireSmeagolSpottedAFreePeoplesSkirmishEventIsCancelledToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var food = scn.GetFreepsCard("food");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var swordarm = scn.GetFreepsCard("swordarm");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(shiresmeagol, aragorn);
		scn.MoveCardsToHand(food, swordarm);
		assertInHand(food, swordarm);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(swordarm);
		// The text names no side: the Free Peoples player's own skirmish event is answered too.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(food));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(swordarm, food);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoSmeagolToSpotItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var food = scn.GetFreepsCard("food");
		var deagol = scn.GetFreepsCard("deagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var drums = scn.GetShadowCard("drums");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(deagol);
		scn.MoveCardsToHand(food);
		scn.MoveCardsToHand(drums);
		assertInHand(food, drums);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, deagol);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		// Gollum, the same creature as Smeagol under another title, and Deagol, a [gollum] Free Peoples card.
		// Gollum is laid once the skirmish is under way, so that he is in no assignment.
		scn.MoveMinionsToTable(gollum);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(drums);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(food));
		assertEquals(7, scn.GetStrength(runner));
		assertInHand(food);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AShadowCardThatIsNoEventPlayedInASkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var food = scn.GetFreepsCard("food");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var haradrim = scn.GetShadowCard("haradrim");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(food);
		scn.MoveCardsToDiscard(mumak);
		assertInHand(food);
		assertInDiscard(mumak);

		scn.StartGame();
		scn.MoveMinionsToTable(haradrim);
		scn.SkipToAssignments();
		scn.SetTwilight(6);
		scn.FreepsAssignToMinions(aragorn, haradrim);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(haradrim));
		scn.ShadowUseCardAction(haradrim);

		// A possession played in the skirmish is no event.
		assertAttachedTo(mumak, haradrim);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(food));
		assertInHand(food);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnEventOfTwoPhasesPlayedInTheManeuverPhaseIsNoSkirmishEventAndIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var food = scn.GetFreepsCard("food");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var flanking = scn.GetShadowCard("flanking");
		var lancers = scn.GetShadowCard("lancers");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(food);
		scn.MoveCardsToHand(flanking);
		assertInHand(food, flanking);

		scn.StartGame();
		scn.MoveMinionsToTable(lancers);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(lancers));
		assertTrue(scn.ShadowPlayAvailable(flanking));
		scn.ShadowPlayCard(flanking);

		// "Maneuver or Skirmish", played in the maneuver phase, is a maneuver event (F-0292).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(food));
		assertEquals(1, scn.GetWoundsOn(lancers));
		assertEquals(pool + 3, scn.GetTwilight());
		assertInDiscard(flanking);
		assertInHand(food);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
