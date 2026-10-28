package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("citadel", "7_33");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: a [gandalf] companion, vitality 4, no text
					// Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4; +2 only beside 3 Ents
					put("avenger", "6_27");
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] companion, vitality 3; an archery ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, vitality 3; +3 only against a Nazgûl
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an [elven] ally, vitality 2, home site 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, vitality 4; a maneuver ability only
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CitadeltoGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Citadel to Gate
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert Gandalf twice to remove all threats and to heal all companions of one culture (except [gandalf]).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("citadel");

		assertEquals("Citadel to Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfTwiceToRemoveAllThreatsAndHealEveryCompanionOfTheCultureChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, avenger, legolas, arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(citadel);
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(avenger, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(citadel);

		scn.StartGame();
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(citadel));
		scn.FreepsPlayCard(citadel);

		// The cost: Gandalf, the one Gandalf, exerts twice; the [gandalf] companion who is no Gandalf does not.
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		// All threats, not one.
		assertEquals(0, scn.GetThreats());

		// The culture is named by a companion of it: any companion who is not [gandalf]; no ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, avenger, silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		// Every [elven] companion healed once; the [elven] ally and the companions of other cultures are not.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInDiscard(citadel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoThreatsItStillHealsTheCultureChosenAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, legolas, aragorn);
		scn.MoveCardsToHand(citadel);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInHand(citadel);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(citadel));
		scn.FreepsPlayCard(citadel);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, aragorn, frodo));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInDiscard(citadel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gandalf, avenger, legolas);
		scn.MoveCardsToHand(citadel);
		// Vitality 4 with 2 wounds: he can exert once but not twice.  The [gandalf] companion beside him could.
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInHand(citadel);

		scn.StartGame();
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(citadel));
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var citadel = scn.GetFreepsCard("citadel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, legolas);
		scn.MoveCardsToHand(citadel);
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInHand(citadel);

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(citadel));
	}
}
