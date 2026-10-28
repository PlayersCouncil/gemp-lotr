package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_223_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("death", "7_223");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, Valiant and no other text
					put("herdsman", "7_247");   // Rohirrim Herdsman: a [rohan] Man ally; a regroup ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					put("mount", "4_287");      // Rider's Mount: a mount, bearer must be a [rohan] Man; its text answers skirmishes only
					put("horse", "4_283");      // Horse of Rohan: a mount, bearer must be a Man, Elf or Wizard; its text answers skirmishes only
					put("sword", "4_291");      // Sword of Rohan: a hand weapon, no mount; bearer must be a [rohan] Man; its text answers skirmishes only

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 and so exhausted; its text answers its own play only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeathTheyCriedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Death They Cried
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a mounted [rohan] Man to exert a minion. That minion's owner may exert a companion and if he or she does so, you may exert a minion; repeat this until a player does not exert a character.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("death");

		assertEquals("Death They Cried", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsAMountedRohanManToExertAMinionAndTheExertionsGoBackAndForthUntilAPlayerStops() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, mount);
		// The Knight is exhausted: vitality 3 with 2 wounds.
		scn.AddWoundsToChar(knight, 2);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, mount);
		assertEquals(2, scn.GetWoundsOn(knight));

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(savage, troop, runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.FreepsPlayAvailable(death));

		scn.FreepsPlayCard(death);
		// The first exertion is no "may".  The Runner, with vitality 1, cannot exert; a companion is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// The Savage's owner may exert a companion: not the ally, and not the exhausted Knight.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(herdsman, knight));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));

		// He did, so the Free Peoples player may exert a minion: any minion, the one exerted already among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, guard));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		// Again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, frodo));
		scn.ShadowChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The Free Peoples player stops: the Shadow player is not asked again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DISCARD, death);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenTheMinionsOwnerDeclinesNothingMoreIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, mount);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(death);
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, frodo));
		scn.ShadowDeclineChoosing();

		// The Free Peoples player is not asked to exert another minion.
		assertInZone(Zone.DISCARD, death);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheBackAndForthStopsWhenNoMinionIsLeftThatCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, mount);

		// The Troop has 1 wound: one exertion leaves it exhausted, and it is the only minion.
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(death);
		// The Troop is the only minion, and is exerted unasked.
		assertEquals(2, scn.GetWoundsOn(troop));

		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, frodo));
		scn.ShadowChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));

		// No minion can exert: the Free Peoples player is not asked, and the Shadow player is not asked again.
		assertInZone(Zone.DISCARD, death);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheMinionsOwnerIsNotAskedWhenNoCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		// Both companions exhausted: the Guard (vitality 3) with 2 wounds, Frodo (vitality 4) with 3.
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, mount);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(3, scn.GetWoundsOn(frodo));

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(death);
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, death);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithEveryMinionExhaustedItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, mount);

		// The Runner has vitality 1, and the Troop 2 wounds of its 3.
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(runner, troop);
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(death));
		scn.FreepsPlayCard(death);

		// Nothing is exerted, and nobody is asked to exert a companion: there is no "that minion" whose owner
		// could be asked.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInZone(Zone.DISCARD, death);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWithAnUnmountedRohanManAndAMountedManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCompanionsToTable(guard, knight);
		scn.AttachCardsTo(guard, sword);
		scn.AttachCardsTo(knight, horse);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, sword, horse);
		assertAttachedTo(sword, guard);
		assertAttachedTo(horse, knight);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		assertInHand(death);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(death));
	}

	@Test
	public void AMountedRohanAllyIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount = scn.GetFreepsCard("mount");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(herdsman, mount);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, mount);
		assertAttachedTo(mount, herdsman);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(death));
		scn.FreepsPlayCard(death);
		// The Troop is the only minion, and is exerted unasked.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo));
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DISCARD, death);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
