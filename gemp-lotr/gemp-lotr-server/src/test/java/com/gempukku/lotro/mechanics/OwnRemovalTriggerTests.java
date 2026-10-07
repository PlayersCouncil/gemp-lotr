package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A card's own "when this card is hindered" or "when this card is discarded" trigger: whose it is.
 *
 * Such a trigger answers the card's removal after the fact, when the card is already hindered or already in
 * the discard pile.  It is the removed card's own text, so it is carried out as that card and for its owner:
 * "you" is the owner, and "this minion" and "another" are counted from the removed card.  Neither is the player
 * or the card that did the removing.
 *
 * Ketura, 2026-10-07 (F-0121), on a game that carried such a trigger out as the player and the card that did
 * the hindering: "Yeah, fix it in the engine."
 *
 * What did the removing is still what a condition on it asks about.  One Ill Turn Deserves Another 103_38,
 * "Each time your condition is hindered or discarded by a Free Peoples card", answers its own hindering or
 * discard (Ketura, 2026-10-06, Q-0075), and only by a Free Peoples card.
 *
 * Ketura, 2026-10-07 (F-0120), on "Each time this minion is hindered" (Desert Wind Scout 103_48): "If the
 * after-hinder works, I'm fine with switching to that".  It does: the trigger of a card that is by then hindered
 * is still offered and carried out.
 *
 * The game keeps four such triggers apart, required and optional, for a hindering and for a discard; each has
 * a test here.
 */
public class OwnRemovalTriggerTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Desert Wind Scout: [raider] minion. Each time this minion is hindered, you may hinder or
					// restore another [raider] minion
					put("scout", "103_48");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man; nothing more below 2 burdens
					put("soldier", "4_254");    // Southron Soldier: [raider] Man; ambush (2) and no other text
					put("nomad", "7_132");      // Desert Nomad: [raider] Man; nothing more below 2 threats
					// One Ill Turn Deserves Another: [isengard] condition. Each time your condition is hindered or
					// discarded by a Free Peoples card, hinder a Free Peoples condition (required)
					put("illturn", "103_38");
					put("traitor", "4_173");    // Saruman, Black Traitor: cost 4, site 4. When you play Saruman, you may discard a condition
					// Orc Footman: [orc] minion. When this minion is discarded from play, you may spot a companion
					// who has resistance 4 or less to shuffle this minion into your draw deck
					put("footman1", "12_93");
					put("footman2", "12_93");
					// Narzug: [gundabad] Orc. When he is discarded, except in the regroup phase, his owner may
					// remove (3) to wound an ally twice
					put("narzug", "30_36");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: Wizard, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: ranger, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf, vitality 3; an archery ability that is never used
					put("albert", "1_69");      // Albert Dreary: ally, vitality 3. Maneuver: exert him to discard an [isengard] or [moria] condition
					put("gaffer", "1_291");     // The Gaffer: ally, vitality 2
					put("enter", "103_9");      // You Cannot Enter Here: Maneuver event. Exert your Wizard to hinder a Shadow card
					put("vengeance", "1_106");  // Gondor's Vengeance: Regroup event. Exert a ranger companion to discard a minion
					put("stand", "1_63");       // Stand Against Darkness: Maneuver event, cost 1. Exert an Elf to discard a [sauron] minion, condition or possession
					put("lordmoria", "1_21");   // Lord of Moria: Free Peoples condition in the support area; each Dwarf is damage +1
					put("stranger", "1_108");   // No Stranger to the Shadows: Free Peoples condition borne by a ranger
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AMinionHinderedByAnOpponentsCardHasItsOwnTriggerOfferedToItsOwnerAndAnotherLeavesOutTheHinderedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var enter = scn.GetFreepsCard("enter");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);

		var scout = scn.GetShadowCard("scout");
		var axeman = scn.GetShadowCard("axeman");
		var soldier = scn.GetShadowCard("soldier");
		var nomad = scn.GetShadowCard("nomad");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, axeman, soldier, nomad);
		// The Nomad is left unhindered, so that hindering is open to the Scout's owner as well.
		scn.HinderCard(axeman, soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(scout));
		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(scout);

		// The Free Peoples player's event did the hindering; the Scout, hindered by now, offers its trigger
		// to its owner.
		assertTrue(scn.IsHindered(scout));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scout));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowChooseOptionalTrigger(scout);

		// "You": the Scout's owner is the one who chooses.
		assertTrue(scn.ShadowChoiceAvailable("Hinder another"));
		assertTrue(scn.ShadowChoiceAvailable("Restore another"));
		assertFalse(scn.FreepsChoiceAvailable("Hinder another"));
		assertFalse(scn.FreepsChoiceAvailable("Restore another"));

		scn.ShadowChoose("Restore another");

		// "Another" is counted from the Scout. The Scout is a hindered [raider] minion itself, and the card
		// that hindered it is no minion at all: counted from that card, the Scout could restore itself.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scout, nomad));
		assertFalse(scn.FreepsHasCardChoiceAvailable(axeman));
		assertFalse(scn.FreepsHasCardChoiceAvailable(soldier));

		scn.ShadowChooseCard(axeman);

		assertFalse(scn.IsHindered(axeman));
		assertTrue(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(scout));
		assertFalse(scn.IsHindered(nomad));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AConditionHinderedByAnOpponentsCardHasItsRequiredTriggerCarriedOutByItsOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		var enter = scn.GetFreepsCard("enter");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);
		scn.MoveCardsToHand(enter);

		var illturn = scn.GetShadowCard("illturn");
		var footman = scn.GetShadowCard("footman1");
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(footman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(illturn));
		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(illturn);

		// The Free Peoples player's event did the hindering; the condition's owner chooses what it hinders.
		assertTrue(scn.IsHindered(illturn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lordmoria, stranger));
		assertFalse(scn.FreepsHasCardChoiceAvailable(lordmoria));
		assertFalse(scn.FreepsHasCardChoiceAvailable(stranger));
		assertFalse(scn.IsHindered(stranger));

		scn.ShadowChooseCard(stranger);

		assertTrue(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(lordmoria));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AConditionDiscardedByAFreePeoplesCardMeetsItsSourceConditionAndItsOwnerCarriesTheTriggerOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var albert = scn.GetFreepsCard("albert");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);

		var illturn = scn.GetShadowCard("illturn");
		var footman = scn.GetShadowCard("footman1");
		scn.MoveCardsToSupportArea(illturn);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, illturn);
		scn.FreepsUseCardAction(albert);

		// "By a Free Peoples card": Albert Dreary is one, and the condition's owner chooses what it hinders.
		assertInZone(Zone.DISCARD, illturn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lordmoria, stranger));
		assertFalse(scn.FreepsHasCardChoiceAvailable(lordmoria));
		assertFalse(scn.FreepsHasCardChoiceAvailable(stranger));
		assertFalse(scn.IsHindered(lordmoria));

		scn.ShadowChooseCard(lordmoria);

		assertTrue(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AConditionDiscardedByItsOwnersShadowCardDoesNotMeetItsSourceCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var stranger = scn.GetFreepsCard("stranger");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lordmoria);
		scn.AttachCardsTo(aragorn, stranger);

		var illturn = scn.GetShadowCard("illturn");
		var traitor = scn.GetShadowCard("traitor");
		scn.MoveCardsToSupportArea(illturn);
		scn.MoveCardsToHand(traitor);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(traitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(traitor));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(illturn);

		// The condition is a Shadow card and so is the card that discarded it: the condition asked for is
		// about the discarding card, a Free Peoples one, and nothing is hindered.
		assertInZone(Zone.DISCARD, illturn);
		assertFalse(scn.IsHindered(lordmoria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionDiscardedByAnOpponentsCardHasItsOwnTriggerOfferedToItsOwnerAndThisMinionIsTheDiscardedOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var vengeance = scn.GetFreepsCard("vengeance");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance);

		var footman1 = scn.GetShadowCard("footman1");
		var footman2 = scn.GetShadowCard("footman2");
		// A copy already in the discard pile, to tell "this minion" from any Orc Footman there.
		scn.MoveCardsToDiscard(footman2);

		scn.StartGame();
		// Frodo's resistance is 10 less the 6 burdens: a companion with resistance 4, as the Footman asks.
		scn.AddBurdens(6);
		scn.MoveMinionsToTable(footman1);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(4, scn.GetResistance(scn.GetRingBearer()));
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPlayCard(vengeance);

		// The Free Peoples player's event did the discarding; the minion's owner is the one asked.
		assertInZone(Zone.DISCARD, footman1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(footman1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();

		// The discarded minion goes into its owner's draw deck: not the event that discarded it, and not the
		// other copy.
		assertInZone(Zone.DECK, footman1);
		assertInZone(Zone.DISCARD, footman2);
		assertInZone(Zone.DISCARD, vengeance);
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AMinionDiscardedByAnOpponentsCardHasItsOwnerPayForAndChooseInItsOwnTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var albert = scn.GetFreepsCard("albert");
		var gaffer = scn.GetFreepsCard("gaffer");
		var stand = scn.GetFreepsCard("stand");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(albert, gaffer);
		scn.MoveCardsToHand(stand);

		var narzug = scn.GetShadowCard("narzug");

		scn.StartGame();
		scn.MoveMinionsToTable(narzug);
		// Stand Against Darkness discards a [sauron] minion.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, narzug, null, Culture.SAURON));
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.MANEUVER);

		// 3 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());

		scn.FreepsPlayCard(stand);

		assertInZone(Zone.DISCARD, narzug);
		// 7 + 1 for the event
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(narzug));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();

		// The owner of the discarded minion pays and chooses, not the player whose event discarded it.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(albert, gaffer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(legolas));
		assertFalse(scn.FreepsHasCardChoiceAvailable(albert));
		assertFalse(scn.FreepsHasCardChoiceAvailable(gaffer));
		assertEquals(0, scn.GetWoundsOn(albert));
		// Legolas exerted to pay for the event.
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.ShadowChooseCard(albert);

		assertEquals(2, scn.GetWoundsOn(albert));
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
