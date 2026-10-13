package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.HinderCardsInPlayEffect;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card that is hindered, or restored, by no card.
 *
 * A text that answers a card being hindered or restored names what did it only where it says so.  "Each time a
 * card is hindered, discard it" answers every hinder; "each time your minion is hindered ... by a Free Peoples
 * card" answers a hinder that a Free Peoples card made, and a hinder that no card made was not made by one.  In a
 * definition that is the `source` of the words `hindered` and `restored`: with none written the trigger asks
 * nothing about what did it, and with one written it is a condition that only a card can meet.
 *
 * The restore that no card makes is the commonest there is, a reconcile's (Comprehensive Rules 5.0, "reconcile":
 * "When any player reconciles (before they discard any cards from hand), restore all cards owned by that
 * player"), and a `restored` trigger used to miss it (F-0361).  HinderedBorneCardTests has that board: Anduril,
 * restored by its player's reconcile, discards the Bow taken up while it was hindered.
 *
 * No card and no rule of the game hinders without a card today, so the hinder here is an effect of the tests' own.
 */
public class HinderedByNoCardTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability never used here
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					// One Ill Turn Deserves Another: an [isengard] condition in the support area.  "Each time
					// your minion is hindered or discarded by a Free Peoples card, hinder a companion."  Its
					// other texts are about its owner's conditions.  (To play, spot 2 [isengard] minions or
					// Saruman: the rig puts it down.)
					put("illturn", "103_38");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "7_356");      // Cross Roads: a Shadow ability that needs 2 [raider] Men
					// Doom Causeway: "Each time a card is hindered, discard it.  Each time a character is
					// exerted, wound it."
					put("site9", "103_127");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ATextThatNamesNothingAsHinderingAnswersAHinderMadeByNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, gimli);

		// The control, away from Doom Causeway: the hinder by itself leaves its card in play
		assertEquals("Cross Roads", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsExecuteAdHocEffect(new HinderCardsInPlayEffect(VirtualTableScenario.P1, null, gimli)); // lint-ok: nothing in the game hinders a card with no card behind it
		assertTrue(scn.IsHindered(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);

		// The fellowship moves to site 9: the Shadow phase
		scn.FreepsPass();
		assertEquals("Doom Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertFalse(scn.IsHindered(aragorn));

		// "Each time a card is hindered, discard it"
		scn.ShadowExecuteAdHocEffect(new HinderCardsInPlayEffect(VirtualTableScenario.P2, null, aragorn)); // lint-ok: the same hinder by no card, where a site answers every hinder
		assertInZone(Zone.DISCARD, aragorn);
		// The one hindered before the fellowship came here is as he was
		assertTrue(scn.IsHindered(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Card_V3_038_Tests has the same minion hindered by a Free Peoples card (You Cannot Enter Here 103_9): the
	// Shadow player then chooses a companion to hinder.
	@Test
	public void ATextThatNamesWhatHinderedDoesNotAnswerAHinderMadeByNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var illturn = scn.GetShadowCard("illturn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Free Peoples player hinders the Shadow player's minion, and no card of his does it
		scn.FreepsExecuteAdHocEffect(new HinderCardsInPlayEffect(VirtualTableScenario.P1, null, troop)); // lint-ok: nothing in the game hinders a card with no card behind it
		assertTrue(scn.IsHindered(troop));

		// Not "by a Free Peoples card": no companion is hindered and the Shadow player is asked for none
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(illturn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
