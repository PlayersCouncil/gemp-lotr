package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wizardry", "4_108");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a Fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("treebeard", "4_103");  // Treebeard, Earthborn: an ally, so a character who is no companion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, Damage +1 only
					put("manofbree", "11_90");  // Man of Bree: [men] Man, strength +1 for each Free Peoples card in hand
					put("goblinman", "2_42");   // Goblin Man: [isengard] Orc, site 2; a Shadow ability only
					put("scimitar", "1_180");   // Goblin Scimitar: a Shadow possession, on the [moria] Orc
					put("ambition", "1_133");   // Saruman's Ambition: a Shadow condition in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WizardryIndeedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wizardry Indeed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> If you can spot more minions than companions, exert Gandalf to make an opponent discard a minion.
		*/

		var scn = GetScenario();

		var wizardry = scn.GetFreepsCard("wizardry");

		assertEquals("Wizardry Indeed", wizardry.getBlueprint().getTitle());
		assertNull(wizardry.getBlueprint().getSubtitle());
		assertFalse(wizardry.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, wizardry.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, wizardry.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, wizardry.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(wizardry, Timeword.MANEUVER));
		assertEquals(4, wizardry.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithOneMoreMinionThanCompanionsGandalfExertsAndTheShadowPlayerChoosesAMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizardry = scn.GetFreepsCard("wizardry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var treebeard = scn.GetFreepsCard("treebeard");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var manofbree = scn.GetShadowCard("manofbree");
		var scimitar = scn.GetShadowCard("scimitar");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(wizardry);

		scn.StartGame();
		// Three minions against two companions (Frodo and Gandalf); counted with the ally they would be three each.
		scn.MoveMinionsToTable(troop, sentry, manofbree);
		scn.AttachCardsTo(troop, scimitar);
		scn.MoveCardsToSupportArea(ambition);
		assertAttachedTo(scimitar, troop);
		assertInZone(Zone.SUPPORT, ambition);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(wizardry));

		scn.FreepsPlayCard(wizardry);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The event waits in the void while the Shadow player chooses.
		assertInZone(Zone.VOID, wizardry);

		// The Shadow player chooses which of his minions goes; his other Shadow cards are not offered.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, sentry, manofbree));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scimitar, ambition));
		scn.ShadowChooseCard(sentry);

		assertInDiscard(sentry, wizardry);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, manofbree);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAsManyMinionsAsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizardry = scn.GetFreepsCard("wizardry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var treebeard = scn.GetFreepsCard("treebeard");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var manofbree = scn.GetShadowCard("manofbree");
		var scimitar = scn.GetShadowCard("scimitar");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(wizardry);

		scn.StartGame();
		// Two minions against two companions; a Shadow possession and a Shadow condition beside them are no minions.
		scn.MoveMinionsToTable(troop, sentry);
		scn.AttachCardsTo(troop, scimitar);
		scn.MoveCardsToSupportArea(ambition);
		assertAttachedTo(scimitar, troop);
		assertInZone(Zone.SUPPORT, ambition);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wizardry));

		// A third minion, and it can be played.
		scn.MoveMinionsToTable(manofbree);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(wizardry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfIsExhaustedThoughAnotherGandalfCompanionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizardry = scn.GetFreepsCard("wizardry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var sentry = scn.GetShadowCard("sentry");
		var manofbree = scn.GetShadowCard("manofbree");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(wizardry);

		scn.StartGame();
		// Four minions against three companions.
		scn.MoveMinionsToTable(troop, sentry, manofbree, goblinman);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wizardry));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// One wound fewer and Gandalf can pay.
		scn.RemoveWoundsFromChar(gandalf, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(wizardry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
