package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spirit", "1_85");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf spotted
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf
					put("sam", "1_311");        // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally, "Fellowship: Exert Elrond to draw a card."
					put("devilry", "5_49");     // Devilry of Orthanc: "Maneuver: Spot 8 [isengard] tokens here to exert every character."
					put("troop", "1_177");      // Goblin Patrol Troop: a minion of vitality 3 with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExertionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spirit", "1_85");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf spotted
					put("sam", "1_311");        // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofSpiritStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Strength of Spirit
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Spell</b>.<br><b>Response:</b> If a companion is about to exert, spot Gandalf to place no token for that exertion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spirit");

		assertEquals("Strength of Spirit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseSpottingGandalfPlacesNoWoundForACompanionsExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertionScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var gandalf = scn.GetFreepsCard("gandalf");
		// The companion who exerts is not Gandalf: "a companion" and the Gandalf spotted are two cards.
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.MoveCardsToHand(spirit);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertInHand(spirit);

		// Sam exerts as the cost of his ability: the response comes before the wound is placed.
		scn.FreepsUseCardAction(sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spirit));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());

		scn.FreepsChooseOptionalTrigger(spirit);
		// No wound for the exertion, which still pays for the burden removed.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, spirit);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var avenger = scn.GetFreepsCard("avenger");
		var sam = scn.GetFreepsCard("sam");
		// Ent Avenger is a [gandalf] companion, but he is not Gandalf.
		scn.MoveCompanionsToTable(avenger, sam);
		scn.MoveCardsToHand(spirit);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertInHand(spirit);

		scn.FreepsUseCardAction(sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(spirit));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertInHand(spirit);
	}

	@Test
	public void IsNotOfferedWhenAnAllyIsAboutToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(spirit);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(spirit);

		scn.FreepsUseCardAction(elrond);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(spirit));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(elrond));
		// The event and the card drawn
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(spirit);
	}

	@Test
	public void WhenEveryCharacterExertsItSparesOneCompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var frodo = scn.GetRingBearer();
		var devilry = scn.GetShadowCard("devilry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(spirit);
		scn.MoveCardsToSupportArea(devilry);
		scn.AddTokensToCard(devilry, 8);

		scn.StartGame();
		assertInHand(spirit);
		assertInZone(Zone.SUPPORT, devilry);
		assertEquals(8, scn.GetCultureTokensOn(devilry));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(8, scn.GetCultureTokensOn(devilry));

		// A Shadow card's effect exerts two companions, an ally and a minion at once.
		scn.ShadowUseCardAction(devilry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spirit));
		scn.FreepsChooseOptionalTrigger(spirit);

		// "That exertion" is one companion's: the companions exerting are offered, the ally and the minion not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(gandalf);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(spirit, devilry);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOnlyCompanionToExertIsAnsweredWhateverHisCultureAndRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spirit = scn.GetFreepsCard("spirit");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var devilry = scn.GetShadowCard("devilry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(spirit);
		scn.MoveCardsToSupportArea(devilry);
		scn.AddTokensToCard(devilry, 8);

		scn.StartGame();
		// Frodo is exhausted and cannot exert: Gandalf, a [gandalf] Wizard, is the one companion who does.
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(troop);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInHand(spirit);
		assertInZone(Zone.SUPPORT, devilry);
		assertEquals(8, scn.GetCultureTokensOn(devilry));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(devilry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spirit));
		scn.FreepsChooseOptionalTrigger(spirit);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(spirit, devilry);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
