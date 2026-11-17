package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("upsmanship", "18_23");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					// Radagast, The Brown: a [gandalf] Wizard who is an ally, the one printed kind of [gandalf] Wizard
					// that is no companion; his fellowship ability is never used here
					put("radagast", "31_17");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; +2 only with 3 Ents
					put("ship", "18_27");       // Ship of Smoke: [gandalf] pipeweed condition; its trigger answers a pipeweed played
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession, support area; a trigger on its own play only
					put("pipe", "1_74");        // Gandalf's Pipe: a pipe, which is no pipeweed; a fellowship ability only

					// Saruman, Keeper of Isengard: a Wizard minion of the [isengard] culture; he may not be assigned
					// and his other texts are about Uruk-hai
					put("saruman", "3_68");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, for a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneUpsmanshipStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: One-Upsmanship
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br>Spot 2 pipeweed cards to remove a burden for each [gandalf] Wizard you can spot.
		*/

		var scn = GetScenario();

		var upsmanship = scn.GetFreepsCard("upsmanship");

		assertEquals("One-Upsmanship", upsmanship.getBlueprint().getTitle());
		assertNull(upsmanship.getBlueprint().getSubtitle());
		assertFalse(upsmanship.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, upsmanship.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, upsmanship.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, upsmanship.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(upsmanship, Keyword.SPELL));
		assertTrue(scn.HasTimeword(upsmanship, Timeword.MANEUVER));
		assertEquals(2, upsmanship.getBlueprint().getTwilightCost());
	}

	@Test
	public void NeedsTwoPipeweedCardsAndRemovesOneBurdenForTheOneGandalfWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var upsmanship = scn.GetFreepsCard("upsmanship");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var pipe = scn.GetFreepsCard("pipe");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.AttachCardsTo(gandalf, pipe);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(upsmanship);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		// One pipeweed upsmanship, beside a pipe that is none: not playable.
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.ATTACHED, pipe);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(upsmanship));

		// A second pipeweed, a condition of another culture.
		scn.MoveCardsToSupportArea(ship);
		assertInZone(Zone.SUPPORT, ship);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(upsmanship));
		assertEquals(3, scn.GetBurdens());

		// Gandalf is the one [gandalf] Wizard: Saruman is a Wizard of another culture and the Ent Avenger a
		// [gandalf] companion who is no Wizard.  3 - 1.
		scn.FreepsPlayCard(upsmanship);
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(upsmanship);
		assertInZone(Zone.SUPPORT, toby, ship);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AGandalfWizardWhoIsAnAllyIsCountedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var upsmanship = scn.GetFreepsCard("upsmanship");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToSupportArea(toby, ship);
		scn.MoveCardsToHand(upsmanship);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(upsmanship));
		assertEquals(3, scn.GetBurdens());

		// Gandalf and Radagast: 3 - 2.
		scn.FreepsPlayCard(upsmanship);
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(upsmanship);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
