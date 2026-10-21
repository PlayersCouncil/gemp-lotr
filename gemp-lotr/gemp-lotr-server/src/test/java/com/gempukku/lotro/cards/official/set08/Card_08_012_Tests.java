package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reckless", "8_12");

					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, archer; Archery: exert him to wound a minion
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, no archer; +3 only against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally (cheated in); a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: Aragorn is an archer; Archery: exert him to wound a minion
					put("bowmen", "3_41");      // Gondor Bowmen: Archery: exert a [gondor] companion and discard it to make the fellowship archery total +2
					put("wreathed", "1_238");   // Wreathed in Shadow: the Shadow player's; Archery: remove (1) to make the fellowship archery total -1

					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, vitality 1, no text
					put("runner", "1_178");     // Goblin Runner: vitality 1; a play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RecklessWeRodeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Reckless We Rode
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Exert an Elf companion and make the fellowship archery total -X (to a minimum of 0) to wound a minion X times. You cannot use archery special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reckless");

		assertEquals("Reckless We Rode", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfCompanionAndTakesXOffTheArcheryTotalToWoundOneMinionXTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, arwen, gimli, aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("bow"));
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(reckless);

		var party = scn.GetShadowCard("party");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertInHand(reckless);
		scn.MoveMinionsToTable(party, troop, brigand);
		scn.SkipToPhase(Phase.ARCHERY);

		// Two archers: Legolas, and Aragorn with his bow.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(reckless));
		scn.FreepsPlayCard(reckless);

		// Elf companions of two titles; not Elrond, an Elf ally, nor Gimli or Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, gimli, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));

		// X runs from 0 to the fellowship archery total.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Minions of three cultures and races, the exhausted Brigand among them; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(party, troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, arwen, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(party);

		// Both wounds on the one minion chosen.
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInDiscard(reckless);

		// The fellowship archery total is 0: archery fire places no wound on a minion.
		scn.PassArcheryActions();
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithXOf1TheArcheryTotalFallsBy1AndAnExhaustedMinionChosenIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("bow"));
		scn.MoveCardsToHand(reckless);

		var party = scn.GetShadowCard("party");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertInHand(reckless);
		scn.MoveMinionsToTable(party, brigand);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(legolas));
		// Legolas, the one Elf companion, exerts unasked.
		scn.FreepsPlayCard(reckless);
		assertEquals(1, scn.GetWoundsOn(legolas));
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// A wound, not an exertion: the exhausted Brigand is offered, and killed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(party, brigand));
		scn.FreepsChooseCard(brigand);
		assertInDiscard(brigand);
		assertEquals(0, scn.GetWoundsOn(party));

		// The 1 left of the fellowship archery total goes on the Raiding Party, the one minion left.
		scn.PassArcheryActions();
		assertEquals(1, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(reckless);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(reckless);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsPlayAvailable(reckless));
	}

	@Test
	public void WithTheOnlyElfCompanionExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		// Elrond, an Elf ally who could exert, is no companion.
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(reckless);
		scn.AddWoundsToChar(legolas, 2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(reckless);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Legolas, exhausted, is still an archer.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(reckless));
	}

	@Test
	public void AfterItTheFreePeoplesPlayerCannotUseArcherySpecialAbilitiesAndTheShadowPlayerStillCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var bowmen = scn.GetFreepsCard("bowmen");
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);
		scn.AttachCardsTo(aragorn, bow);
		scn.MoveCardsToSupportArea(bowmen);
		scn.MoveCardsToHand(reckless);

		var wreathed = scn.GetShadowCard("wreathed");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wreathed);

		scn.StartGame();
		assertInHand(reckless);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// A character's, an item's and a condition's archery special abilities, all offered before the event.
		assertTrue(scn.FreepsActionAvailable(legolas));
		assertTrue(scn.FreepsActionAvailable(bow));
		assertTrue(scn.FreepsActionAvailable(bowmen));
		scn.FreepsPlayCard(reckless);
		scn.FreepsChooseCard(arwen);
		scn.FreepsChoose("0");
		assertEquals(2, scn.GetFreepsArcheryTotal());

		// The Shadow player's archery special ability is still his to use.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wreathed));
		scn.ShadowUseCardAction(wreathed);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// None of the Free Peoples player's.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertFalse(scn.FreepsActionAvailable(bow));
		assertFalse(scn.FreepsActionAvailable(bowmen));
	}

	@Test
	public void TheBarOnArcherySpecialAbilitiesEndsWithTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reckless = scn.GetFreepsCard("reckless");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.MoveCardsToHand(reckless);

		var runner = scn.GetShadowCard("runner");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		assertInHand(reckless);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsPlayCard(reckless);
		scn.FreepsChooseCard(arwen);
		scn.FreepsChoose("0");
		scn.ShadowPass();
		// Still barred at the Free Peoples player's next decision of the phase.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));

		// Legolas's archery wound kills the Runner; a second move, to site 3, for a second archery phase this turn.
		scn.PassArcheryActions();
		assertInDiscard(runner);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(party);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(legolas));
	}
}
