package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guides", "17_9");

					// Elves, and followers.
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("calaglin", "3_10");    // Calaglin, Elf of Lórien: [elven] Elf ally; a regroup ability
					put("uruviel", "15_26");    // Uruviel, Woodland Maid: [elven] follower; its Aid and transfer trigger only
					put("worker", "15_135");    // Rohan Worker: [rohan] follower; its Aid and transfer trigger only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man who is no Elf; a maneuver ability used only on purpose

					// Free Peoples conditions and a possession.
					put("future", "11_26");     // Uncertain Future: [elven] support-area condition; a maneuver ability used only on purpose
					put("stairs", "1_24");      // Stairs of Khazad-dûm: [dwarven] support-area condition; only touches a Dwarf's skirmish
					put("athelas", "1_94");     // Athelas: [gondor] possession borne by a [gondor] Man; a fellowship ability

					// Shadow conditions, in a support area and on a companion, and a possession.
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] support-area condition; [isengard] events cost 1 less, a skirmish ability
					put("armory", "1_173");     // Goblin Armory: [moria] support-area condition; answers a [moria] weapon played or a [moria] Orc's wound
					put("drawn", "1_211");      // Drawn to Its Power: [wraith] support-area condition; answers a kill in a Nazgûl's skirmish
					put("few", "1_260");        // The Number Must Be Few: [sauron] support-area condition; reads 7 companions
					put("defense", "1_244");    // Desperate Defense of the Ring: [sauron] condition on a companion; answers his assignment
					put("poison", "8_28");      // Spider Poison: [gollum] support-area possession; a skirmish ability for Shelob's skirmish

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LothlorienGuidesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Lothlórien Guides
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot 2 Elves (or an Elf and an [elven] follower) to discard a condition (or discard two conditions if you can spot 4 or more Shadow conditions).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guides");

		assertEquals("Lothlórien Guides", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TwoElvesDiscardOneConditionOfEitherSideAndNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guides = scn.GetFreepsCard("guides");
		var aragorn = scn.GetFreepsCard("aragorn");
		var future = scn.GetFreepsCard("future");
		var stairs = scn.GetFreepsCard("stairs");
		var athelas = scn.GetFreepsCard("athelas");
		var ambition = scn.GetShadowCard("ambition");
		var armory = scn.GetShadowCard("armory");
		var defense = scn.GetShadowCard("defense");
		var poison = scn.GetShadowCard("poison");
		// An Elf companion and an Elf ally.
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable("calaglin");
		scn.MoveCardsToSupportArea(future, stairs, ambition, armory, poison);
		scn.AttachCardsTo(aragorn, athelas, defense);
		scn.MoveCardsToHand(guides);
		assertInZone(Zone.SUPPORT, future, stairs, ambition, armory, poison);
		assertAttachedTo(athelas, aragorn);
		assertAttachedTo(defense, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(guides));
		scn.FreepsPlayCard(guides);

		// Three Shadow conditions, one short of four: one condition, of either side, in a support area or on a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(future, stairs, ambition, armory, defense));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(athelas, poison));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(defense);

		assertInDiscard(defense);
		assertInZone(Zone.SUPPORT, future, stairs, ambition, armory, poison);
		assertAttachedTo(athelas, aragorn);
		assertInDiscard(guides);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFourShadowConditionsTwoConditionsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guides = scn.GetFreepsCard("guides");
		var future = scn.GetFreepsCard("future");
		var ambition = scn.GetShadowCard("ambition");
		var armory = scn.GetShadowCard("armory");
		var drawn = scn.GetShadowCard("drawn");
		var few = scn.GetShadowCard("few");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveAlliesToTable("calaglin");
		scn.MoveCardsToSupportArea(future, ambition, armory, drawn, few);
		scn.MoveCardsToHand(guides);
		assertInZone(Zone.SUPPORT, future, ambition, armory, drawn, few);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(guides);

		// Exactly two, with nothing asked first: the parenthesis is done once it can be.  Either side's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(future, ambition, armory, drawn, few));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(future, ambition);

		assertInDiscard(future, ambition);
		assertInZone(Zone.SUPPORT, armory, drawn, few);
		assertInDiscard(guides);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnElfAndAnElvenFollowerAlsoLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guides = scn.GetFreepsCard("guides");
		var uruviel = scn.GetFreepsCard("uruviel");
		var future = scn.GetFreepsCard("future");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCardsToSupportArea(uruviel, future, ambition);
		scn.MoveCardsToHand(guides);
		assertInZone(Zone.SUPPORT, uruviel, future, ambition);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		// The follower's Aid is offered as the phase starts.
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.FreepsPlayAvailable(guides));
		scn.FreepsPlayCard(guides);
		assertTrue(scn.FreepsHasCardChoiceAvailable(future, ambition));
		scn.FreepsChooseCard(ambition);

		assertInDiscard(ambition);
		assertInZone(Zone.SUPPORT, future, uruviel);
		assertInDiscard(guides);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneElfAndNoElvenFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guides = scn.GetFreepsCard("guides");
		var uruviel = scn.GetFreepsCard("uruviel");
		var worker = scn.GetFreepsCard("worker");
		var future = scn.GetFreepsCard("future");
		var ambition = scn.GetShadowCard("ambition");
		// One Elf, a companion who is no Elf, a follower of another culture and an [elven] card that is no follower.
		scn.MoveCompanionsToTable("legolas", "aragorn");
		scn.MoveCardsToSupportArea(worker, future, ambition);
		scn.MoveCardsToHand(guides);
		assertInZone(Zone.SUPPORT, worker, future, ambition);
		assertInHand(guides);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		// The follower's Aid is offered as the phase starts.
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(guides));

		scn.MoveCardsToSupportArea(uruviel);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(guides));
	}

	@Test
	public void WithNoConditionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guides = scn.GetFreepsCard("guides");
		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable("calaglin");
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveCardsToSupportArea(poison);
		scn.MoveCardsToHand(guides);
		assertAttachedTo(athelas, aragorn);
		assertInZone(Zone.SUPPORT, poison);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(guides));
		scn.FreepsPlayCard(guides);

		assertInDiscard(guides);
		assertAttachedTo(athelas, aragorn);
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
