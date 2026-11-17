package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("healing", "3_22");
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: an Elrond who is an ally; heals the allies whose home is site 3 at the start of each of your turns
					put("elrondcomp", "12_17");   // Elrond, Witness to History: an Elrond who is a companion; a skirmish ability only
					put("orophin", "1_56");       // Orophin, Lórien Bowman: [elven] ally, home 6, vitality 3; an archery ability used only on purpose
					put("rumil", "1_57");         // Rúmil, Elven Protector: [elven] ally, home 6, vitality 3; an archery ability used only on purpose
					put("albert", "1_69");        // Albert Dreary, Entertainer from Bree: [gandalf] ally, home 1, vitality 3; a maneuver ability used only on purpose
					put("legolas", "1_50");       // Legolas, Greenleaf: [elven] companion who is no ally; an archery ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion, vitality 4; a maneuver ability used only on purpose

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MasterofHealingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Master of Healing
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship or Regroup
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Regroup:</b> Spot Elrond (or 2 [elven] allies) to heal a companion or ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("healing");

		assertEquals("Master of Healing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheFellowshipPhaseElrondTheAllyLetsItHealOneWoundFromACompanionOrAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healing = scn.GetFreepsCard("healing");
		var elrond = scn.GetFreepsCard("elrond");
		var albert = scn.GetFreepsCard("albert");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		// Elrond is the only [elven] ally, so the two-allies half of the requirement is not met and only his title lets it be played.
		scn.MoveAlliesToTable(elrond, albert);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(healing);

		scn.StartGame();
		// Wounded after the start of the turn, where Elrond's own heal (of allies whose home is site 3) is past.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(albert, 2);

		assertInZone(Zone.HAND, healing);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(albert));
		assertTrue(scn.FreepsPlayAvailable(healing));
		scn.FreepsPlayCard(healing);

		// A companion and allies of two cultures; the unwounded Ring-bearer is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, elrond, albert));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elrond);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(albert));
		assertInDiscard(healing);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheRegroupPhaseTwoElvenAlliesLetItHealAndAWoundedMinionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healing = scn.GetFreepsCard("healing");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var albert = scn.GetFreepsCard("albert");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(orophin, rumil, albert);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(healing);

		var savage = scn.GetShadowCard("savage");

		assertInZone(Zone.HAND, healing);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(albert, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(albert));
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		scn.SkipToPhase(Phase.REGROUP);

		// No Elrond: Orophin and Rúmil are the two [elven] allies.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.HAND, healing);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(albert));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(healing));
		scn.FreepsPlayCard(healing);

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, albert));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));
		scn.FreepsChooseCard(albert);

		assertEquals(1, scn.GetWoundsOn(albert));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(healing);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneElvenAllyAndNoElrondItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healing = scn.GetFreepsCard("healing");
		var orophin = scn.GetFreepsCard("orophin");
		var albert = scn.GetFreepsCard("albert");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(orophin, albert);
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(healing);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.FreepsRefreshDecision();

		// One [elven] ally who is no Elrond, beside an [elven] companion and an ally of another culture.
		assertInZone(Zone.HAND, healing);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(healing));
	}

	@Test
	public void ItCannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healing = scn.GetFreepsCard("healing");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(healing);

		var troop = scn.GetShadowCard("troop");
		assertInZone(Zone.HAND, healing);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, healing);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(healing));
	}

	@Test
	public void WithElrondTheCompanionAndNobodyWoundedItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var healing = scn.GetFreepsCard("healing");
		var elrond = scn.GetFreepsCard("elrondcomp");
		var orophin = scn.GetFreepsCard("orophin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(elrond);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(healing);

		scn.StartGame();

		// Elrond is spotted, and no companion or ally has a wound to heal: an event is played for what it can do.
		assertInZone(Zone.HAND, healing);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(healing));
		scn.FreepsPlayCard(healing);

		assertInDiscard(healing);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
